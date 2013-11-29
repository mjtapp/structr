/**
 * Copyright (C) 2010-2013 Axel Morgner, structr <structr@structr.org>
 *
 * This file is part of structr <http://structr.org>.
 *
 * structr is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * structr is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with structr.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.structr.core.property;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import org.apache.lucene.search.BooleanClause;
import org.structr.common.SecurityContext;
import org.structr.common.error.FrameworkException;
import org.structr.core.GraphObject;
import org.structr.core.PropertyGroup;
import org.structr.core.app.StructrApp;
import org.structr.core.converter.PropertyConverter;
import org.structr.core.entity.AbstractRelationship;
import org.structr.core.graph.search.PropertySearchAttribute;
import org.structr.core.graph.search.SearchAttribute;
import org.structr.core.graph.search.SearchAttributeGroup;

/**
 * A property that returns grouped properties from a set of {@link Reference} elements.
 * 
 * @author Christian Morgner
 */
public class ReferenceGroup extends Property<PropertyMap> implements PropertyGroup<PropertyMap> {

	private static final Logger logger = Logger.getLogger(ReferenceGroup.class.getName());

	// indicates whether this group property is 
	protected Map<String, PropertyKey> propertyKeys    = new LinkedHashMap<>();
	protected Class<? extends GraphObject> entityClass = null;
	protected Property<Boolean> nullValuesOnlyProperty = null;
	
	public ReferenceGroup(String name, Class<? extends GraphObject> entityClass, Reference... properties) {
		
		super(name);
		
		for (PropertyKey key : properties) {
			propertyKeys.put(key.jsonName(), key);
		}
		
		this.nullValuesOnlyProperty = new BooleanProperty(name.concat(".").concat("nullValuesOnly"));
		this.entityClass            = entityClass;

		// register in entity context
		// FIXME StructrApp.getConfiguration().registerProperty(entityClass, nullValuesOnlyProperty);
		StructrApp.getConfiguration().registerPropertyGroup(entityClass, this, this);	
	}
	
	// ----- interface PropertyGroup -----
	@Override
	public PropertyMap getGroupedProperties(SecurityContext securityContext, GraphObject source) {

		if(source instanceof AbstractRelationship) {

			AbstractRelationship rel = (AbstractRelationship)source;
			PropertyMap properties   = new PropertyMap();

			for (PropertyKey key : propertyKeys.values()) {

				Reference reference = (Reference)key;

				GraphObject referencedEntity = reference.getReferencedEntity(rel);
				PropertyKey referenceKey     = reference.getReferenceKey();
				PropertyKey propertyKey      = reference.getPropertyKey();
				
				if (referencedEntity != null) {
					
					properties.put(propertyKey, referencedEntity.getProperty(referenceKey));
				}
			}
			
			return properties;
		}
		
		return null;
	}

	@Override
	public void setGroupedProperties(SecurityContext securityContext, PropertyMap source, GraphObject destination) throws FrameworkException {

		if(destination instanceof AbstractRelationship) {

			AbstractRelationship rel = (AbstractRelationship)destination;

			for (PropertyKey key : propertyKeys.values()) {

				Reference reference = (Reference)key;
				
				GraphObject referencedEntity = reference.getReferencedEntity(rel);
				PropertyKey referenceKey     = reference.getReferenceKey();
				PropertyKey propertyKey      = reference.getPropertyKey();
				
				if (referencedEntity != null && !reference.isReadOnly()) {
					
					Object value = source.get(propertyKey);
					referencedEntity.setProperty(referenceKey, value);
				}
			}
		}
	}
	
	@Override
	public String typeName() {
		return "Object";
	}
	
	@Override
	public PropertyConverter<PropertyMap, ?> databaseConverter(SecurityContext securityContext) {
		return null;
	}

	@Override
	public PropertyConverter<PropertyMap, ?> databaseConverter(SecurityContext securityContext, GraphObject currentObject) {
		return null;
	}

	@Override
	public PropertyConverter<Map<String, Object>, PropertyMap> inputConverter(SecurityContext securityContext) {
		return new InputConverter(securityContext);
	}

	@Override
	public SearchAttribute getSearchAttribute(SecurityContext securityContext, BooleanClause.Occur occur, PropertyMap searchValues, boolean exactMatch) {
		
		SearchAttributeGroup group = new SearchAttributeGroup(occur);
		
		for (PropertyKey key : propertyKeys.values()) {
			
			Object value = searchValues.get(new GenericProperty(key.jsonName()));
			if (value != null) {
				
				group.add( new PropertySearchAttribute(key, value.toString(), BooleanClause.Occur.MUST, exactMatch) );
			}
		}
		
		return group;
	}

	/**
	 * Returns the nested group property for the given name. The PropertyKey returned by
	 * this method can be used to get and/or set the property value in a PropertyMap that
	 * is obtained or stored in the group property.
	 * 
	 * @param <T>
	 * @param name
	 * @param type
	 * @return 
	 */
	public <T> PropertyKey<T> getNestedProperty(String name, Class<T> type) {
		
		if (!propertyKeys.containsKey(name)) {
			throw new IllegalArgumentException("ReferenceGroup " + dbName + " does not contain grouped property " + name + "!");
		}
		
		return propertyKeys.get(name);
	}
	
	/**
	 * Returns a wrapped group property that can be used to access a nested group
	 * property directly, i.e. without having to fetch the group first.
	 * 
	 * @param <T>
	 * @param name
	 * @param type
	 * @return 
	 */
	public <T> PropertyKey<T> getDirectAccessReferenceGroup(String name, Class<T> type) {
		
		if (!propertyKeys.containsKey(name)) {
			throw new IllegalArgumentException("ReferenceGroup " + dbName + " does not contain grouped property " + name + "!");
		}
		
		return new GenericProperty(propertyKeys.get(name).dbName());
	}
	
	private class InputConverter extends PropertyConverter<Map<String, Object>, PropertyMap> {

		public InputConverter(SecurityContext securityContext) {
			super(securityContext, null);
		}
		
		@Override
		public Map<String, Object> revert(PropertyMap source) throws FrameworkException {
			return PropertyMap.javaTypeToInputType(securityContext, entityClass, source);
		}

		@Override
		public PropertyMap convert(Map<String, Object> source) throws FrameworkException {
			return PropertyMap.inputTypeToJavaType(securityContext, entityClass, source);
		}
	}
	
	@Override
	public Object fixDatabaseProperty(Object value) {
		return null;
	}
	
	@Override
	public PropertyMap getProperty(SecurityContext securityContext, GraphObject obj, boolean applyConverter) {
		return getGroupedProperties(securityContext, obj);
	}
	
	@Override
	public void setProperty(SecurityContext securityContext, GraphObject obj, PropertyMap value) throws FrameworkException {
		setGroupedProperties(securityContext, value, obj);
	}
	
	@Override
	public Class relatedType() {
		return null;
	}
	
	@Override
	public boolean isCollection() {
		return false;
	}

	@Override
	public Integer getSortType() {
		return null;
	}
	
	@Override
	public void setDeclaringClass(Class declaringClass) {
		
		for (PropertyKey key : propertyKeys.values()) {

			key.setDeclaringClass(declaringClass);
		}
	}

	@Override
	public void index(GraphObject entity, Object value) {

		for (PropertyKey key : propertyKeys.values()) {

			key.index(entity, entity.getPropertyForIndexing(key));
		}
	}
		
	@Override
	public List<SearchAttribute> extractSearchableAttribute(SecurityContext securityContext, HttpServletRequest request, boolean looseSearch) throws FrameworkException {

		List<SearchAttribute> searchAttributes = new LinkedList<SearchAttribute>();
		
		for (PropertyKey key : propertyKeys.values()) {

			searchAttributes.addAll(key.extractSearchableAttribute(securityContext, request, looseSearch));
		}
		
		return searchAttributes;
	}

	@Override
	public Object getValueForEmptyFields() {
		return null;
	}
	
}
