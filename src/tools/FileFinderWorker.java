package tools;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.SwingWorker;

import holders.Configuration;
import hud.Popups;
import hud.UpdatableTableModel;

public class FileFinderWorker extends SwingWorker<List<File>, File> {

    private UpdatableTableModel model;

    public FileFinderWorker(UpdatableTableModel inTModel) {
        this.model = inTModel;
    }

    /* DnD File Adding Stage 1 */
    @Override
    protected List<File> doInBackground() throws Exception {
        // You could actually publish the entire array, but I'm doing this deliberately ;)
        List<File> processList = model.getProcessingFileList();
        
        // sort before adding to list
        Collections.sort(processList);
        
        int size = processList.size();
        for (int i = 0; i < size; i++) {
        	publish(processList.get(i));
		}
        
        return processList;
    }
    
    /* DnD File Adding Stage 2 */
    @Override
    protected void process(List<File> filesList) {
    	
    	// adding files as rows in 'UpdatableTableModel' Class
        int size = filesList.size();
        for (int i = 0; i < size; i++) {
        	model.addFile(filesList.get(i));
		}
        
        // show errors what may have been appear
        Popups.showRuntimeErrorsLog();
    }

    /* DnD File Adding Stage 3 */
    // after added all files to 'inside' background preprocessor
    @Override
    protected void done() {
    	/* do here something after 'publishing files', without any controllable pause */
    }
}