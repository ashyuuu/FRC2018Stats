import java.awt.*;
import java.beans.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

public class RowNumberTable extends JTable 
	implements ChangeListener, PropertyChangeListener, TableModelListener {
	
	private JTable main;
	
	public RowNumberTable(JTable table){
		main = table;
		main.addPropertyChangeListener(this);
		main.getModel().addTableModelListener(this);
		setFocusable( false );
		setAutoCreateColumnsFromModel(false);
		setSelectionModel(main.getSelectionModel());
		TableColumn column = new TableColumn();
		column.setHeaderValue(" ");
		addColumn(column);
		column.setCellRenderer(new RowNumberRenderer());
		getColumnModel().getColumn(0).setPreferredWidth(50);
		setPreferredScrollableViewportSize(getPreferredSize());
	}
	

	@Override
	public void addNotify(){
		super.addNotify();
		Component c = getParent();
		
		//Keep scrolling of the row table in sync with the main table.
		if (c instanceof JViewport){
			JViewport viewport = (JViewport)c;
			viewport.addChangeListener(this);
		}
	}

	//delegate method to main table
	@Override
	public int getRowCount(){
		return main.getRowCount();
	}

	@Override
	public int getRowHeight(int row){
		int rowHeight = main.getRowHeight(row);
		if (rowHeight != super.getRowHeight(row))
			super.setRowHeight(row, rowHeight);
		return rowHeight;
	}

	//no model is being used for this table so just use the row number as the value of the cell
	@Override
	public Object getValueAt(int row, int column){
		return Integer.toString(row + 1);
	}

	//didn't edit data in the main TableModel by mistake
	@Override
	public boolean isCellEditable(int row, int column){
		return false;
	}

	//do nothing since the table ignores the model
	@Override
	public void setValueAt(Object value, int row, int column) {}

	//implement the change listener
	public void stateChanged(ChangeEvent e){
		//  Keep the scrolling of the row table in sync with main table
		JViewport viewport = (JViewport) e.getSource();
		JScrollPane scrollPane = (JScrollPane)viewport.getParent();
		scrollPane.getVerticalScrollBar().setValue(viewport.getViewPosition().y);
	}

	//implement the property change listener
	public void propertyChange(PropertyChangeEvent e){
		//  Keep the row table in sync with the main table
		if ("selectionModel".equals(e.getPropertyName())) setSelectionModel( main.getSelectionModel() );
		if ("rowHeight".equals(e.getPropertyName())) repaint();	
		if ("model".equals(e.getPropertyName())) {
			main.getModel().addTableModelListener( this );
			revalidate();
		}
	}

	//implement the table model listener
	@Override
	public void tableChanged(TableModelEvent e){
		revalidate();
	}

	//attempt to mimic the table header renderer
	private static class RowNumberRenderer extends DefaultTableCellRenderer{
		public RowNumberRenderer(){
			setHorizontalAlignment(JLabel.CENTER);
		}
		public Component getTableCellRendererComponent(
			JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column){
			if (table != null){
				JTableHeader header = table.getTableHeader();
				if (header != null){
					setForeground(header.getForeground());
					setBackground(header.getBackground());
					setFont(header.getFont());
				}
			}
			if (isSelected)	setFont( getFont().deriveFont(Font.BOLD) );
			setText((value == null) ? "" : value.toString());
			setBorder(UIManager.getBorder("TableHeader.cellBorder"));
			return this;
		}
	}
}