if(!dojo._hasResource["dojox.form.manager._DisplayMixin"]){ //_hasResource checks added by build. Do not use _hasResource directly in your code.
dojo._hasResource["dojox.form.manager._DisplayMixin"] = true;
dojo.provide("dojox.form.manager._DisplayMixin");

dojo.declare("dojox.form.manager._DisplayMixin", null, {
	// summary:
	//		Form manager's mixin for controlling show/hide state of
	//		controlled elements (defined by dojoAttachPoint attributes).
	// description:
	//		This mixin provides unified show/hide functionality for
	//		controlled elements (indicated by dojoAttachPoint attribute).
	//		Essentially it provides a way to change "style.display"
	//		parameter of controlled nodes.
	//		It should be used together with dojox.form.manager.Mixin.

	gatherDisplayState: function(names){
		// summary:
		//		Gather display state of all attached elements and return as a dictionary.
		// names: Object?:
		//		If it is an array, it is a list of names to be processed.
		//		If it is an object, dictionary keys are names to be processed.
		//		If it is omitted, all known attach point nodes are to be processed.

		var result = this.inspectAttachedPoints(function(name, node){
			return dojo.style(node, "display") != "none";
		}, names);

		return result;	// Object
	},

	show: function(state, defaultState){
		// summary:
		//		Show attached nodes according to the supplied state object.
		// state: Object?:
		//		Optional. If a name-value dictionary, the value is true
		//		to show and false to hide. If an array, all names in the
		//		array will be set to defaultState. If omitted, all form
		//		elements will be set to defaultState.
		// defaultState: Boolean?:
		//		The default state (true, if omitted).

		if(arguments.length < 2){
			defaultState = true;
		}

		this.inspectAttachedPoints(function(name, node, value){
			dojo.style(node, "display", value ? "" : "none");
		}, state, defaultState);

		return this;	// self
	},

	hide: function(state){
		// summary:
		//		Hide attached nodes according to the supplied state object.
		// state: Object?:
		//		Optional. If a name-value dictionary, the value is true
		//		to show and false to hide. If an array, all names in the
		//		array will be hidden. If omitted, all form elements
		//		will be hidden.
		return this.show(state, false);	// self
	}
});

}
