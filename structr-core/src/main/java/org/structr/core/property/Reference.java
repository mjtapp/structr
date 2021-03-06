/**
 * Copyright (C) 2010-2014 Morgner UG (haftungsbeschränkt)
 *
 * This file is part of Structr <http://structr.org>.
 *
 * Structr is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Structr is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Structr.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.structr.core.property;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.apache.lucene.search.BooleanClause.Occur;
import org.structr.common.SecurityContext;
import org.structr.common.error.FrameworkException;
import org.structr.core.GraphObject;
import org.structr.core.PropertyValidator;
import org.structr.core.app.Query;
import org.structr.core.converter.PropertyConverter;
import org.structr.core.entity.AbstractRelationship;
import org.structr.core.graph.NodeService;
import org.structr.core.graph.search.SearchAttribute;

/**
 * Contains information about a related node property. This class can be used together
 * with {@link ReferenceGroup} to return a group of properties from both start and end
 * node of a relationship.
 * 
 * @author Christian Morgner
 */
public class Reference<T> implements PropertyKey<T> {

	public enum Key {
		StartNode, Relationship, EndNode
	}

	private PropertyKey<T> referenceKey = null;
	private PropertyKey<T> propertyKey  = null;
	private Key referenceType           = null;
	
	public Reference(PropertyKey propertyKey, Key referenceType, PropertyKey<T> referenceKey) {
		this.referenceType = referenceType;
		this.referenceKey = referenceKey;
		this.propertyKey = propertyKey;
	}
	
	public PropertyKey<T> getReferenceKey() {
		return referenceKey;
	}

	public PropertyKey<T> getPropertyKey() {
		return propertyKey;
	}

	public GraphObject getReferencedEntity(AbstractRelationship relationship) {
		
		if (relationship != null) {

			switch (referenceType) {

				case StartNode:
					return relationship.getSourceNode();

				case Relationship:
					return relationship;

				case EndNode:
					return relationship.getTargetNode();
			}
		}
		
		return null;
	}
	
	// interface PropertyKey
	@Override
	public String dbName() {
		return propertyKey.dbName();
	}

	@Override
	public String jsonName() {
		return propertyKey.jsonName();
	}
	
	@Override
	public void dbName(String dbName) {
		propertyKey.dbName(dbName);
	}

	@Override
	public void jsonName(String jsonName) {
		propertyKey.jsonName(jsonName);
	}
	
	@Override
	public String typeName() {
		return propertyKey.typeName();
	}

	@Override
	public T defaultValue() {
		return propertyKey.defaultValue();
	}

	@Override
	public Integer getSortType() {
		return propertyKey.getSortType();
	}

	@Override
	public PropertyConverter<T, ?> databaseConverter(SecurityContext securityContext) {
		return databaseConverter(securityContext, null);
	}

	@Override
	public PropertyConverter<T, ?> databaseConverter(SecurityContext securityContext, GraphObject entity) {
		return propertyKey.databaseConverter(securityContext, entity);
	}

	@Override
	public PropertyConverter<?, T> inputConverter(SecurityContext securityContext) {
		return propertyKey.inputConverter(securityContext);
	}

	@Override
	public Class<? extends GraphObject> relatedType() {
		return propertyKey.relatedType();
	}

	@Override
	public boolean isUnvalidated() {
		return propertyKey.isUnvalidated();
	}

	@Override
	public boolean isReadOnly() {
		return propertyKey.isReadOnly();
	}

	@Override
	public boolean isWriteOnce() {
		return propertyKey.isWriteOnce();
	}

	@Override
	public boolean isIndexed() {
		return propertyKey.isIndexed();
	}

	@Override
	public boolean isPassivelyIndexed() {
		return propertyKey.isPassivelyIndexed();
	}

	@Override
	public boolean isSearchable() {
		return propertyKey.isSearchable();
	}

	@Override
	public boolean isIndexedWhenEmpty() {
		return propertyKey.isIndexedWhenEmpty();
	}

	@Override
	public boolean isCollection() {
		return propertyKey.isCollection();
	}

	@Override
	public void setDeclaringClass(Class declaringClass) {
	}
	
	@Override
	public Class<? extends GraphObject> getDeclaringClass() {
		return propertyKey.getDeclaringClass();
	}

	@Override
	public T getProperty(SecurityContext securityContext, GraphObject obj, boolean applyConverter) {
		return getProperty(securityContext, obj, applyConverter, null);
	}

	@Override
	public T getProperty(SecurityContext securityContext, GraphObject obj, boolean applyConverter, final org.neo4j.helpers.Predicate<GraphObject> predicate) {
		return propertyKey.getProperty(securityContext, obj, applyConverter);
	}

	@Override
	public void setProperty(SecurityContext securityContext, GraphObject obj, T value) throws FrameworkException {
		propertyKey.setProperty(securityContext, obj, value);
	}

	@Override
	public SearchAttribute getSearchAttribute(SecurityContext securityContext, Occur occur, T searchValue, boolean exactMatch, final Query query) {
		return propertyKey.getSearchAttribute(securityContext, occur, searchValue, exactMatch, query);
	}

	@Override
	public void registrationCallback(Class entityType) {
	}

	@Override
	public void addValidator(PropertyValidator<T> validator) {
		propertyKey.addValidator(validator);
	}

	@Override
	public List<PropertyValidator<T>> getValidators() {
		return propertyKey.getValidators();
	}
	
	@Override
	public boolean requiresSynchronization() {
		return false;
	}
	
	@Override
	public String getSynchronizationKey() {
		return null;
	}

	@Override
	public void index(GraphObject entity, Object value) {
		propertyKey.index(entity, value);
	}

	@Override
	public void extractSearchableAttribute(SecurityContext securityContext, HttpServletRequest request, final Query query) throws FrameworkException {
		propertyKey.extractSearchableAttribute(securityContext, request, query);
	}

	@Override
	public T convertSearchValue(SecurityContext securityContext, String requestParameter) throws FrameworkException {
		return propertyKey.convertSearchValue(securityContext, requestParameter);
	}

	@Override
	public Property<T> indexed() {
		return propertyKey.indexed();
	}

	@Override
	public Property<T> indexed(NodeService.NodeIndex nodeIndex) {
		return propertyKey.indexed(nodeIndex);
	}

	@Override
	public Property<T> indexed(NodeService.RelationshipIndex relIndex) {
		return propertyKey.indexed(relIndex);
	}

	@Override
	public Property<T> passivelyIndexed() {
		return propertyKey.passivelyIndexed();
	}

	@Override
	public Property<T> passivelyIndexed(NodeService.NodeIndex nodeIndex) {
		return propertyKey.passivelyIndexed(nodeIndex);
	}

	@Override
	public Property<T> passivelyIndexed(NodeService.RelationshipIndex relIndex) {
		return propertyKey.passivelyIndexed(relIndex);
	}

	@Override
	public Property<T> indexedWhenEmpty() {
		return propertyKey.indexedWhenEmpty();
	}

	@Override
	public int getProcessingOrderPosition() {
		return 0;
	}
}
