package com.dotcms.publishing.bundlers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.dotcms.publishing.BundlerStatus;
import com.dotcms.publishing.BundlerUtil;
import com.dotcms.publishing.DotBundleException;
import com.dotcms.publishing.IBundler;
import com.dotcms.publishing.PublisherConfig;
import com.dotcms.publishing.PublisherUtil;
import com.dotmarketing.beans.Host;
import com.dotmarketing.beans.Identifier;
import com.dotmarketing.beans.VersionInfo;
import com.dotmarketing.business.APILocator;
import com.dotmarketing.business.IdentifierAPI;
import com.dotmarketing.business.UserAPI;
import com.dotmarketing.business.VersionableAPI;
import com.dotmarketing.exception.DotDataException;
import com.dotmarketing.portlets.contentlet.business.ContentletAPI;
import com.dotmarketing.portlets.folders.business.FolderAPI;
import com.dotmarketing.portlets.htmlpages.business.HTMLPageAPI;
import com.dotmarketing.portlets.htmlpages.model.HTMLPage;
import com.dotmarketing.util.Logger;
import com.dotmarketing.util.UtilMethods;
import com.liferay.portal.model.User;

public class StaticHTMLPageBundler implements IBundler {

	private PublisherConfig config;
	ContentletAPI conAPI = null;
	UserAPI uAPI = null;
	FolderAPI fAPI = null;
	IdentifierAPI iAPI = null;
	HTMLPageAPI pAPI = null;
	VersionableAPI vAPI = null;
	User systemUser = null;
	
	public final static String HTML_ASSET_EXTENSION = ".dothtml.xml" ;
	
	@Override
	public String getName() {
		return "Static HTML Page Bundler";
	}
	
	@Override
	public void setConfig(PublisherConfig pc) {
		config = pc;
		conAPI = APILocator.getContentletAPI();
		uAPI = APILocator.getUserAPI();
		fAPI = APILocator.getFolderAPI();
		iAPI = APILocator.getIdentifierAPI();
		pAPI = APILocator.getHTMLPageAPI();
		vAPI = APILocator.getVersionableAPI();
		try {
			systemUser = uAPI.getSystemUser();
		} catch (DotDataException e) {
			Logger.fatal(this,e.getMessage(),e);
		}
	}

	@Override
	public void generate(File bundleRoot, BundlerStatus status) throws DotBundleException{
		boolean include = true;
		boolean hasPatterns = false;
		List<String> patterns = null;
		List<Identifier> pageIdents = new ArrayList<Identifier>();
		List<Identifier> deletedIdents = new ArrayList<Identifier>();
		
		if(config.getExcludePatterns()!=null && config.getExcludePatterns().size()>0){
			hasPatterns = true;
			include = false;
			patterns = config.getExcludePatterns();
		}else if(config.getIncludePatterns()!=null && config.getIncludePatterns().size()>0){
			hasPatterns = true;
			include = true;
			patterns = config.getIncludePatterns();
		}
				
		try{
			for(Host h : config.getHosts()){
				if(!hasPatterns){
					try{
						//live
						pageIdents.addAll(iAPI.findByURIPattern(new HTMLPage().getType(), "/*",true ,false,include, h, config.getStartDate(), config.getEndDate()));
						//working
						pageIdents.addAll(iAPI.findByURIPattern(new HTMLPage().getType(), "/*",false,false,include, h, config.getStartDate(), config.getEndDate()));

					}catch (NullPointerException e) {}
				}else{
					for(String pattern : patterns){
						try{
							//live
							pageIdents.addAll(iAPI.findByURIPattern(new HTMLPage().getType(),pattern ,true ,false, include, h, config.getStartDate(), config.getEndDate()));
							//working (including deleted)
							pageIdents.addAll(iAPI.findByURIPattern(new HTMLPage().getType(),pattern ,false,false, include, h, config.getStartDate(), config.getEndDate()));

						}catch (NullPointerException e) {}
					}
				}

				status.setTotal(pageIdents.size());
				for (Identifier i : pageIdents) {


					
					try{
						HTMLPageWrapper w = new HTMLPageWrapper();
						VersionInfo vi = vAPI.getVersionInfo(i.getId());
						w.setIdentifier(i);
						w.setVersionInfo(vi);

						
						List<HTMLPage> pages = new ArrayList<HTMLPage>();
						pages.add(pAPI.loadWorkingPageById(i.getId(), uAPI.getSystemUser(), true));
						if(UtilMethods.isSet(vi.getLiveInode())){
							pages.add(pAPI.loadLivePageById(i.getId(), uAPI.getSystemUser(), true));
						}
						for(HTMLPage p : pages){
							w.setPage(p);
							writeFileToDisk(bundleRoot,w, i.getURI(), h);
							status.addCount();
						}
						
						
					}catch(Exception e){
						Logger.error(this, e.getMessage() + " : Unable to get HTMLPage to write to bundle", e);
						status.addFailure();
						continue;
					}

				}
			}
		}catch (DotDataException e) {
			Logger.error(this, e.getMessage() + " : Unable to get Pages for Start HTML Bundler",e);
		}
	}
	
	private void writeFileToDisk(File bundleRoot, HTMLPageWrapper htmlPageWrapper, String uri, Host h) throws IOException, DotBundleException{
		if(uri == null){
			Logger.warn(this, "URI is not set for Bundler to write");
			return;
		}
		
		boolean live = (htmlPageWrapper.getPage().getInode().equals(htmlPageWrapper.getVersionInfo().getLiveInode() )) ;
		
		
		try{
			String wrapperFile = bundleRoot.getPath() + File.separator 
					+ (live ? "live" : "working") + File.separator 
					+ h.getHostname() 
					+ uri.replace("/", File.separator) + HTML_ASSET_EXTENSION;
			File wf = new File(wrapperFile);
			
			String staticFile = bundleRoot.getPath() + File.separator 
					+ (live ? "live" : "working") + File.separator 
					+ h.getHostname() 
					+ uri.replace("/", File.separator);
			File sf = new File(staticFile);
			
			// Should we write or is the file already there:
			Calendar cal = Calendar.getInstance();
			cal.setTime(htmlPageWrapper.getVersionInfo().getVersionTs());
			cal.set(Calendar.MILLISECOND, 0);
			
			String dir = wrapperFile.substring(0, wrapperFile.lastIndexOf(File.separator));
			new File(dir).mkdirs();
			
			if(!wf.exists() || wf.lastModified() != cal.getTimeInMillis()){
				BundlerUtil.objectToXML(htmlPageWrapper, wf);
				// set the time of the file
				wf.setLastModified(cal.getTimeInMillis());
			}
			
			if(!sf.exists() || sf.lastModified() != cal.getTimeInMillis()){
				try {
					
					BufferedWriter out = null;
					try{
						if(!sf.exists())sf.createNewFile();
						FileWriter fstream = new FileWriter(sf);
						out = new BufferedWriter(fstream);
						String html = new String();
						html = pAPI.getHTML(htmlPageWrapper.getIdentifier().getURI(), h,live , null, uAPI.getSystemUser());
						out.write(html);
						out.close();
						sf.setLastModified(cal.getTimeInMillis());
					}catch(Exception e){
						Logger.error(this, e.getMessage() + " Unable to get page : " + htmlPageWrapper.getIdentifier().getHostId() + htmlPageWrapper.getIdentifier().getURI());
					}
					finally{
						if(out !=null){
							out.close();
						}
					}
					
					
				} catch (FileNotFoundException e) {
					Logger.error(PublisherUtil.class,e.getMessage(),e);
				}catch (IOException e) {
					Logger.error(PublisherUtil.class,e.getMessage(),e);
				}
			}
		}
		catch(Exception e){
			throw new DotBundleException("cant get host for " + uri + " reason " + e.getMessage());
		}
	}


	@Override
	public FileFilter getFileFilter(){
		return new StaticHTMLBundlerFilter();
	}
	
	public class StaticHTMLBundlerFilter implements FileFilter{

		@Override
		public boolean accept(File pathname) {

			return (pathname.isDirectory() || pathname.getName().endsWith(HTML_ASSET_EXTENSION));
		}

	}

}
