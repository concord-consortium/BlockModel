package org.concord.waba.extra.event;



import waba.ui.Event;

import waba.ui.Control;

import waba.sys.Vm;



public class ActionEvent extends Event

{

public Object source;

String actionCommand;

	public ActionEvent(Object source, Control c,String actionCommand){

		this.source = source;

		this.actionCommand = actionCommand;

		target = c;

		timeStamp = Vm.getTimeStamp();

	}

	

    public Object getSource() {return source;}

    /**

     * Returns the command name associated with this action.

     */

    public String getActionCommand() {

        return actionCommand;

    }

}

