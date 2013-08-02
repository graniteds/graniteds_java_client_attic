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

package org.granite.client.validation.javafx;

import java.util.List;

import org.granite.client.validation.javafx.ValidationResult;

import javafx.event.Event;
import javafx.event.EventTarget;
import javafx.event.EventType;

/**
 * @author William DRAI
 */
public class ValidationResultEvent extends Event {

	private static final long serialVersionUID = 1L;
	
	public static EventType<ValidationResultEvent> ANY = new EventType<ValidationResultEvent>(Event.ANY);
	public static EventType<ValidationResultEvent> VALID = new EventType<ValidationResultEvent>(ANY, "valid");
	public static EventType<ValidationResultEvent> INVALID = new EventType<ValidationResultEvent>(ANY, "invalid");
	public static EventType<ValidationResultEvent> UNHANDLED = new EventType<ValidationResultEvent>(ANY, "unhandled");
	

	private final List<ValidationResult> errorResults;
	
	public ValidationResultEvent(Object source, EventTarget target, EventType<ValidationResultEvent> type, List<ValidationResult> errorResults) {
		super(source, target, type);
		this.errorResults = errorResults;
	}

	public List<ValidationResult> getErrorResults() {
		return errorResults;
	}
}
