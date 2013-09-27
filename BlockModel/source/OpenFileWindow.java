import waba.ui.*;
import extra.ui.*;
import waba.io.*;
import waba.sys.*;

public class OpenFileWindow extends Window
{
    Button load, lCancel, recv;
    Button save, sCancel, send;
    ListBox files = new ListBox();
    Container loadCont, saveCont, curCont;
    Edit fileEdit;

    public OpenFileWindow()
    {
	super("Files", true);
	
       	setRect(CENTER,TOP, 150, 150);

	loadCont = new Container();
	loadCont.setRect(2,130,146,18);
	load = new Button("Load");
	load.setRect(2,0,PREFERRED, PREFERRED); 
	loadCont.add(load);
	recv = new Button("IR Recv");
	recv.setRect(40,0,PREFERRED, PREFERRED); 
	loadCont.add(recv);
	lCancel=new Button("Cancel");
	lCancel.setRect(100,0,PREFERRED, PREFERRED); 
	loadCont.add(lCancel);


	saveCont = new Container();
	saveCont.setRect(2,110,146,38);

	fileEdit = new Edit();
	fileEdit.setRect(2,0, 100, 18);
	saveCont.add(fileEdit);

	save = new Button("Save");
	save.setRect(2,20,PREFERRED, PREFERRED); 
	saveCont.add(save);
       
	send = new Button("IR Send");
	send.setRect(40,20,PREFERRED, PREFERRED); 
	saveCont.add(send);
       
	sCancel = new Button("Cancel");
	sCancel.setRect(90,20,PREFERRED, PREFERRED); 
	saveCont.add(sCancel);

	curCont = null;

    }

    String curFileName = null;
    BlockModel bm;
    File curDir = null;
    File sendsDir = null;

    public void updateFiles()
    {
	// Create the save dir if not created
	curDir = new File("saves", File.DONT_OPEN);
	if(!curDir.isDir()){
	    curDir.createDir();
	}

	// Create the sends dir if not created
	sendsDir = new File("sends", File.DONT_OPEN);
	if(!sendsDir.isDir()){
	    sendsDir.createDir();
	}

	String [] filesList = curDir.listDir();

	remove(files);
	files = new ListBox();
	files.setRect(2,15,width-4,80);
	add(files);

	if(filesList != null){
	    for(int i=0; i<filesList.length; i++){
		files.add(filesList[i]);
	    }
	}

    }

    public void load(BlockModel bm)
    {
	this.bm = bm;
	updateFiles();

	if(curCont != loadCont){
	    if(curCont != null){
		remove(curCont);
	    }
	    add(loadCont);
	    curCont = loadCont;
	}

	MainWindow.getMainWindow().popupModal(this);
	
    }

    public void save(BlockModel bm, String curFile)
    {
	this.bm = bm;

	updateFiles();

	if(curCont != saveCont){
	    if(curCont != null){
		remove(curCont);
	    }
	    add(saveCont);
	    curCont = saveCont;
	}

	fileEdit.setText(curFile);

	MainWindow.getMainWindow().popupModal(this);	

    }

    MessageBox confirmReplace = null;
    String [] confRepButtons = {"Yes", "No"};

    public void onEvent(Event e)
    {
	if(e.type == ControlEvent.WINDOW_CLOSED){
	    if(e.target == confirmReplace){
		if(confirmReplace.getPressedButtonIndex() == 0){
		    bm.save(curDir.getPath() + "/", fileEdit.getText());
		    unpop();
		} 
	    }	    
	} else 
	if(e.type == ControlEvent.PRESSED){
	    if(e.target == files){
		fileEdit.setText(files.getSelected());
	    } else if(e.target == lCancel){
		unpop();
	    } else if(e.target == sCancel){
		unpop();
	    } else if(e.target == load){		
		bm.load(curDir.getPath() + "/", files.getSelected());
		unpop();
	    } else if(e.target == save){
		File saveFile = new File(curDir.getPath() + "/" + fileEdit.getText(), 
					 File.DONT_OPEN);
		if(saveFile.exists()){
		    confirmReplace = new MessageBox("Are you sure?",
						    fileEdit.getText() + " already exists.|Do " +
						    "you want to replace it?",
						    confRepButtons);
		    popupModal(confirmReplace);
		    return;
		}
		bm.save(curDir.getPath() + "/", fileEdit.getText());
		unpop();
	    } else if(e.target == recv){
		// Get a directory list beforehand
		File myDocDir = new File("\\My Documents", File.DONT_OPEN);
		String [] beforeList = myDocDir.listDir();
		Vm.exec("irsquirt.exe", null, 0, true);
		String [] afterList = myDocDir.listDir();
		int i,j;
		boolean noMatch;
		String newFileName = null;

		for(i=0; i<afterList.length; i++){
		    noMatch = true;
		    for(j=0; j<beforeList.length; j++){
			if(afterList[i].equals(beforeList[j])){
			    noMatch = false;
			    break;
			}
		    }
		    if(noMatch == true){
			newFileName = afterList[i];
			break;
		    }
		}
		
		File newFile = new File("\\My Documents\\" + newFileName,
					File.DONT_OPEN);
		bm.load("/My Documents/", newFileName);
		newFile.delete();
		unpop();
	    } else if(e.target == send){
		bm.save(sendsDir.getPath() + "/", fileEdit.getText());

		Vm.exec("irsquirt.exe", "\\" + sendsDir.getPath() + "\\" + fileEdit.getText(), 0, true);
		unpop();
	    }

	}
    }
}














