/*
  GRANITE DATA SERVICES
  Copyright (C) 2012 GRANITE DATA SERVICES S.A.S.

  This file is part of Granite Data Services.

  Granite Data Services is free software; you can redistribute it and/or modify
  it under the terms of the GNU Library General Public License as published by
  the Free Software Foundation; either version 2 of the License, or (at your
  option) any later version.

  Granite Data Services is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
  for more details.

  You should have received a copy of the GNU Library General Public License
  along with this library; if not, see <http://www.gnu.org/licenses/>.
*/

package org.granite.client.tide.collections.javafx;

import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.SortType;
import javafx.scene.control.TableView;

/**
 * @author William DRAI
 */
public class TableViewSort<S> implements Sort {
	
	private TableView<S> tableView;
	private S exampleData;
	
	private String[] order = new String[0];
	private boolean[] desc = new boolean[0];

	public TableViewSort(final TableView<S> tableView, final Class<S> exampleDataClass) {
		this.tableView = tableView;
		try {
			this.exampleData = exampleDataClass.newInstance();
		}
		catch (Exception e) {
			throw new RuntimeException("Could not instantiate example data class " + exampleDataClass, e);
		}
	}
	
	public void setTableView(TableView<S> tableView) {
		this.tableView = tableView;
	}
	
	public void build() {
		int i = 0;
		order = new String[tableView.getSortOrder().size()];
		desc = new boolean[tableView.getSortOrder().size()];
		for (TableColumn<S, ?> column : tableView.getSortOrder()) {
			ObservableValue<?> property = column.getCellObservableValue(exampleData);
			if (property instanceof ReadOnlyProperty<?>) {
				order[i] = ((ReadOnlyProperty<?>)property).getName();
				desc[i] = column.getSortType() == SortType.DESCENDING;
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
