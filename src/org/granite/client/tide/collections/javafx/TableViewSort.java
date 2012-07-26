package org.granite.client.tide.collections.javafx;

import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.SortType;
import javafx.scene.control.TableView;


public class TableViewSort<S> implements Sort {
	
	private TableView<S> tableView;
	private S exampleData;
	
	private String[] order = new String[0];
	private boolean[] desc = new boolean[0];

	public TableViewSort(final TableView<S> tableView, final S exampleData) {
		this.tableView = tableView;
		this.exampleData = exampleData;
	}
	
	public void build() {
		int i = 0;
		order = new String[tableView.getSortOrder().size()];
		desc = new boolean[tableView.getSortOrder().size()];
		for (TableColumn<S, ?> column : tableView.getSortOrder()) {
			ObservableValue<?> property = column.getCellObservableValue(exampleData);
			if (property instanceof ReadOnlyProperty<?>) {
				order[i] = ((ReadOnlyProperty<?>)property).getName();
				desc[i] = column.getSortType() == SortType.ASCENDING;
				i++;
			}
			else
				throw new IllegalArgumentException("Call values must implement Property to apply TableViewSort adapter");
		}
	}
	
	public String[] getOrder() {
		return order;
	}
	
	public boolean[] getDesc() {
		return desc;
	}
}
