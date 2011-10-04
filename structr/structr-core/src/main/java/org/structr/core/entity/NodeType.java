/*
 *  Copyright (C) 2011 Axel Morgner, structr <structr@structr.org>
 * 
 *  This file is part of structr <http://structr.org>.
 * 
 *  structr is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  structr is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with structr.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.structr.core.entity;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.StringUtils;
import org.structr.common.RenderMode;
import org.structr.core.NodeRenderer;
import org.structr.core.NodeSource;

/**
 * A NodeType node defines the type of all connected nodes.
 * 
 * The properties of a node define the possible properties of all nodes of this type.
 * 
 * 
 * @author axel
 */
public class NodeType extends AbstractNode implements NodeSource
{
	private static final Logger logger = Logger.getLogger(NodeType.class.getName());
	private static final String DEFINITION_SEPARATOR =		"=";
	
	public static final String PREFIX =				"_";
	public static final String SEPARATOR =				",";

	public enum Definition {
		
		fieldType, inputType, name, label, size, maxLength, rows, columns, required
	}
	
	// cached results
	Map<String, InputField> inputFieldMap = new LinkedHashMap<String, InputField>();
	
	public InputField getInputField(String propertyKey) {
		
		InputField inputField = inputFieldMap.get(propertyKey);
		if(inputField == null) {
			
			// new property definition, parse values
			inputField = parsePropertyDefinition(propertyKey, getStringProperty(propertyKey));
			inputFieldMap.put(propertyKey, inputField);
		}
		
		return(inputField);
	}
	
	@Override
	public String getIconSrc()
	{
		return "/images/database_table.png";
	}

	@Override
	public void onNodeCreation()
	{
	}

	@Override
	public void onNodeInstantiation()
	{
	}

	@Override
	public void initializeRenderers(final Map<RenderMode, NodeRenderer> rendererMap)
	{
	}

	@Override
	public void onNodeDeletion()
	{
	}

	@Override
	public AbstractNode loadNode()
	{
		return(this);
	}
	
	// ----- private methods -----
	private InputField parsePropertyDefinition(String propertyKey, String propertyDefinition) {
		
		// new property definition, multiple values
		String[] listElements = propertyDefinition.split("[,]+");
		InputFieldImpl inputField = new InputFieldImpl();
		
		// set name of field
		inputField.setName(propertyKey.substring(PREFIX.length()));
		
		if(listElements.length > 1) {

			for(int i=0; i<listElements.length; i++) {

				String element = listElements[i].trim();
				if(element.length() > 0 && element.contains(DEFINITION_SEPARATOR)) {
					
					parseDefinition(element, inputField);
				}
			}
			
		} else {
			
			if(propertyDefinition.contains(DEFINITION_SEPARATOR)) {
				
				// new definition with only one element
				parseDefinition(propertyDefinition, inputField);
				
			} else {
				
				// old definition, only type is set
				inputField.setFieldType(propertyDefinition);
			}
		}

		return(inputField);
	}
	
	private void parseDefinition(String element, InputFieldImpl inputField) {
		
		String[] definitionParts = element.split("[=]+");
		if(definitionParts.length == 2) {

			// finally there..
			String key = definitionParts[0].trim();
			String val = definitionParts[1].trim();

			if(key.length() > 0 && val.length() > 0) {

				try {
					Definition def = Definition.valueOf(key);
					switch(def) {

						case fieldType:		inputField.setFieldType(val); break;
						case inputType:		inputField.setInputType(val); break;
						case name:		inputField.setName(val); break;
						case label:		inputField.setLabel(val); break;
						case size:		inputField.setSize(val); break;
						case maxLength:		inputField.setMaxLength(val); break;
						case rows:		inputField.setRows(val); break;
						case columns:		inputField.setColumns(val); break;
						case required:		inputField.setRequired(val); break;
					}

				} catch(Throwable t) {

					logger.log(Level.WARNING, "Unsupported custom node type property {0}", key);
				}
			}
		}
	}
	
	// ----- nested classes -----
	public interface InputField {
		
		public String getFieldType();
		public String getInputType();
		public String getName();
		public String getLabel();
		public int getSize();
		public int getMaxLength();
		public int getRows();
		public int getColumns();
		public boolean isRequired();
	}
	
	private class InputFieldImpl implements InputField {

		private String fieldType = "java.lang.String";
		private String inputType = null;
		private String name = null;
		private String label = null;
		private int size = 20;
		private int maxLength = -1;
		private int rows = 0;
		private int columns = 80;
		private boolean required = false;
		
		public InputFieldImpl() {
		}

		/**
		 * @return the fieldType
		 */
		@Override
		public String getFieldType() {
			return fieldType;
		}

		/**
		 * @param fieldType the fieldType to set
		 */
		public void setFieldType(String fieldType) {
			this.fieldType = fieldType;
		}

		/**
		 * @return the inputType
		 */
		@Override
		public String getInputType() {
			return inputType;
		}

		/**
		 * @param inputType the inputType to set
		 */
		public void setInputType(String inputType) {
			this.inputType = inputType;
		}

		/**
		 * @return the name
		 */
		@Override
		public String getName() {
			return name;
		}

		/**
		 * @param name the name to set
		 */
		public void setName(String name) {
			this.name = name;
		}

		/**
		 * @return the label
		 */
		@Override
		public String getLabel() {
			
			if(label == null) {
			
				return(StringUtils.capitalize(name));
			}
			
			return label;
		}

		/**
		 * @param label the label to set
		 */
		public void setLabel(String label) {
			this.label = label;
		}

		/**
		 * @return the maxLength
		 */
		@Override
		public int getMaxLength() {
			return maxLength;
		}

		/**
		 * @param maxLength the maxLength to set
		 */
		public void setMaxLength(String maxLength) {
			
			int value = -1;
			
			try { value = Integer.parseInt(maxLength); } catch(Throwable t) {}
			
			this.maxLength = value;
		}

		/**
		 * @return the rows
		 */
		@Override
		public int getRows() {
			return rows;
		}

		/**
		 * @param rows the rows to set
		 */
		public void setRows(String rows) {
			
			int value = -1;
			
			try { value = Integer.parseInt(rows); } catch(Throwable t) {}
			
			this.rows = value;
		}

		/**
		 * @return the columns
		 */
		@Override
		public int getColumns() {
			return columns;
		}

		/**
		 * @param columns the columns to set
		 */
		public void setColumns(String columns) {
			
			int value = -1;
			
			try { value = Integer.parseInt(columns); } catch(Throwable t) {}
			
			this.columns = value;
		}

		/**
		 * @return the required
		 */
		@Override
		public boolean isRequired() {
			return required;
		}

		/**
		 * @param required the required to set
		 */
		public void setRequired(String required) {

			boolean value = false;
			
			if("true".equalsIgnoreCase(required))	value = true;	else
			if("yes".equalsIgnoreCase(required))	value = true;	else
			if("ja".equalsIgnoreCase(required))	value = true;	else
			if("1".equalsIgnoreCase(required))	value = true;
			
			this.required = value;
		}

		/**
		 * @return the size
		 */
		@Override
		public int getSize() {
			return size;
		}

		/**
		 * @param size the size to set
		 */
		public void setSize(String size) {
			int value = -1;
			
			try { value = Integer.parseInt(size); } catch(Throwable t) {}
			
			this.size = value;
		}
	}
}