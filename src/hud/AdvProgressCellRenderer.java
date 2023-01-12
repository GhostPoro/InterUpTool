package hud;

import java.awt.Component;

import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

public class AdvProgressCellRenderer extends JProgressBar implements TableCellRenderer {
	
	private static final long serialVersionUID = 1L;

	public AdvProgressCellRenderer() {
		// Initialize the progress bar renderer to use a horizontal
		// progress bar.
		super(JProgressBar.HORIZONTAL);

		// Ensure that the progress bar border is not painted. (The
		// result is ugly when it appears in a table cell.)
		setBorderPainted(false);

		// Ensure that percentage text is painted on the progress bar.
		setStringPainted(true);
	}

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
        int progress = 0;
        if (value instanceof Float) {
            progress = Math.round(((Float) value) * 100f);
        } else if (value instanceof Integer) {
            progress = (int) value;
        }
        setValue(progress);
		return this;
	}
}
