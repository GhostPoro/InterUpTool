package hud;

import java.util.List;

import holders.RowData;

public class SimpleAnimator {
	
	private static boolean running = false;
	
	private static boolean needAnimate = false;

	public static boolean run(UpdatableTableModel model, List<RowData> rows) {
		
		if(!running && rows.size() > 0) {
			
			Thread animator = new Thread() {
				public void run() {
					int size = rows.size();
					do {
						needAnimate = false;
						for (int i = 0; i < size; i++) {
							RowData row = rows.get(i);
							if(!row.isPropertiesSet()) {
								needAnimate = true;
								model.setFileProperties(row.getCheckSum(), row.getFileProperties(), false);
							}
						}
						try { Thread.sleep(1000); } catch (InterruptedException ie) { ie.printStackTrace(); }
						int currentSize = rows.size();
						if(currentSize != size) {
							needAnimate = true;
							size = currentSize;
						}
					} while(needAnimate);
					running = false;
					System.out.println("SimpleAnimator Finished!");
				}
			};
			animator.start();
			running = true;
		}
		return false;
	}

}
