/**
 * Copyright [2014] Gaurav Gupta
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.netbeans.jpa.modeler.core.widget.attribute;

import java.awt.Cursor;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.lang.model.SourceVersion;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import org.apache.commons.lang3.StringUtils;
import org.atteo.evo.inflector.English;
import org.netbeans.jcode.core.util.JavaSourceHelper;
import org.netbeans.jpa.modeler.core.widget.FlowPinWidget;
import org.netbeans.jpa.modeler.core.widget.JavaClassWidget;
import org.netbeans.jpa.modeler.core.widget.OpenSourceCodeAction;
import org.netbeans.jpa.modeler.properties.PropertiesHandler;
import static org.netbeans.jpa.modeler.properties.PropertiesHandler.getAttributeAnnoation;
import static org.netbeans.jpa.modeler.properties.PropertiesHandler.getAttributeSnippet;
import static org.netbeans.jpa.modeler.properties.PropertiesHandler.getFieldTypeProperty;
import static org.netbeans.jpa.modeler.properties.PropertiesHandler.getJaxbVarTypeProperty;
import org.netbeans.jpa.modeler.rules.attribute.AttributeValidator;
import org.netbeans.jpa.modeler.rules.entity.SQLKeywords;
import org.netbeans.jpa.modeler.settings.view.AttributeViewAs;
import org.netbeans.jpa.modeler.settings.view.ViewPanel;
import org.netbeans.jpa.modeler.spec.EmbeddedId;
import org.netbeans.jpa.modeler.spec.IdentifiableClass;
import org.netbeans.jpa.modeler.spec.NamedNativeQuery;
import org.netbeans.jpa.modeler.spec.NamedQuery;
import org.netbeans.jpa.modeler.spec.extend.Attribute;
import org.netbeans.jpa.modeler.spec.extend.CollectionTypeHandler;
import org.netbeans.jpa.modeler.spec.extend.MapKeyHandler;
import org.netbeans.jpa.modeler.spec.extend.MapKeyType;
import org.netbeans.jpa.modeler.spec.jaxb.JaxbVariableTypeHandler;
import org.netbeans.jpa.modeler.specification.model.scene.JPAModelerScene;import org.netbeans.modeler.specification.model.document.IModelerScene;
import static org.netbeans.jpa.modeler.specification.model.util.JPAModelerUtil.DELETE_ICON;
import org.netbeans.modeler.core.ModelerFile;
import org.netbeans.modeler.specification.model.document.property.ElementPropertySet;
import org.netbeans.modeler.widget.node.IPNodeWidget;
import static org.netbeans.modeler.widget.node.IWidgetStateHandler.StateType.ERROR;
import static org.netbeans.modeler.widget.node.IWidgetStateHandler.StateType.WARNING;
import org.netbeans.modeler.widget.pin.info.PinWidgetInfo;
import org.netbeans.modeler.widget.properties.generic.ElementCustomPropertySupport;
import org.netbeans.modeler.widget.properties.handler.PropertyChangeListener;
import org.netbeans.modeler.widget.properties.handler.PropertyVisibilityHandler;
import org.netbeans.modules.j2ee.persistence.dd.JavaPersistenceQLKeywords;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.WindowManager;

/**
 *
 * @author Gaurav Gupta
 * @param <E>
 */
public abstract class AttributeWidget<E extends Attribute> extends FlowPinWidget<E, JPAModelerScene> {

    public AttributeWidget(JPAModelerScene scene, IPNodeWidget nodeWidget, PinWidgetInfo pinWidgetInfo) {
        super(scene, nodeWidget, pinWidgetInfo);
    }
    
    public void visualizeDataType() {
        AttributeViewAs viewAs = ViewPanel.getDataType();
        String dataType = this.getBaseElementSpec().getDataTypeLabel();
        if (null != viewAs) switch (viewAs) {
            case SIMPLE_CLASS_NAME:
                dataType = JavaSourceHelper.getSimpleClassName(dataType);
                break;
            case SHORT_CLASS_NAME:
                dataType = JavaSourceHelper.getSimpleClassName(dataType);
                final int SHORT_LENGTH = 3;
                if (dataType.length() > SHORT_LENGTH) {
                    dataType = dataType.substring(0, SHORT_LENGTH);
                }   break;
            case NONE:
                return;
            default:
                break;
        }
        visualizeDataType(dataType);
    }

    @Override
    public void createPropertySet(ElementPropertySet set) {
        super.createPropertySet(set);
        Attribute attribute = this.getBaseElementSpec();
        if (!(attribute instanceof EmbeddedId)) {//to hide property
            set.put("ATTR_PROP", getFieldTypeProperty("fieldType", "Field Type", "", false, this));
        } else {
            try {//add "custom manual editable class type property" instead of "Field Type Panel" for EmbeddedId
                set.put("JPA_PROP", new ElementCustomPropertySupport(set.getModelerFile(), this.getClassWidget().getBaseElementSpec(), String.class,
                       "compositePrimaryKeyClass", "compositePrimaryKeyClass", "Field Type", "", null));
            } catch (NoSuchMethodException | NoSuchFieldException ex) {
                this.getModelerScene().getModelerFile().handleException(ex);;
            }
        }
        getJaxbVarTypeProperty(set, this, (JaxbVariableTypeHandler) attribute);
        set.put("ATTR_PROP", getAttributeAnnoation(this.getModelerScene(), attribute.getAnnotation()));
        set.put("ATTR_PROP", getAttributeSnippet(this.getModelerScene(), attribute.getSnippets()));

        
        attribute.getAttributeConstraints().forEach((constraint) -> {
            set.createPropertySet("ATTRIBUTE_CONSTRAINTS", "ATTRIBUTE_CONSTRAINTS", this, constraint);
        }); 
        attribute.getKeyConstraints().forEach((constraint) -> {
            set.createPropertySet("KEY_CONSTRAINTS", "KEY_CONSTRAINTS", this, constraint);
        }); 
        attribute.getValueConstraints().forEach((constraint) -> {
            set.createPropertySet("VALUE_CONSTRAINTS", "VALUE_CONSTRAINTS", this, constraint);
        }); 

        this.addPropertyChangeListener("name", (PropertyChangeListener<String>) (oldValue, value) -> {
            if (StringUtils.isBlank(value)) {
                JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(), NbBundle.getMessage(AttributeValidator.class, AttributeValidator.EMPTY_ATTRIBUTE_NAME));
                setName(AttributeWidget.this.getLabel());//rollback
            } else {
                setName(value);
                setLabel(value);
            }
        });

        this.addPropertyChangeListener("table_name", (PropertyChangeListener<String>) (oldValue, tableName) -> {
            if (tableName != null && !tableName.trim().isEmpty()) {
                if (SQLKeywords.isSQL99ReservedKeyword(tableName)) {
                    getSignalManager().fire(WARNING, AttributeValidator.ATTRIBUTE_TABLE_NAME_WITH_RESERVED_SQL_KEYWORD);
                } else {
                    getSignalManager().clear(WARNING, AttributeValidator.ATTRIBUTE_TABLE_NAME_WITH_RESERVED_SQL_KEYWORD);
                }
            } else {
                getSignalManager().clear(WARNING, AttributeValidator.ATTRIBUTE_TABLE_NAME_WITH_RESERVED_SQL_KEYWORD);
            }
        });
        this.addPropertyChangeListener("column_name", (PropertyChangeListener<String>) (oldValue, columnName) -> {
            if (columnName != null && !columnName.trim().isEmpty()) {
                if (SQLKeywords.isSQL99ReservedKeyword(columnName)) {
                    getSignalManager().fire(WARNING, AttributeValidator.ATTRIBUTE_COLUMN_NAME_WITH_RESERVED_SQL_KEYWORD);
                } else {
                    getSignalManager().clear(WARNING, AttributeValidator.ATTRIBUTE_COLUMN_NAME_WITH_RESERVED_SQL_KEYWORD);
                }
            } else {
                getSignalManager().clear(WARNING, AttributeValidator.ATTRIBUTE_COLUMN_NAME_WITH_RESERVED_SQL_KEYWORD);
            }
        });
    }

    @Override
    protected List<JMenuItem> getPopupMenuItemList() {
        List<JMenuItem> menuList = super.getPopupMenuItemList();

        JMenuItem delete;
        delete = new JMenuItem("Delete");
        delete.setIcon(DELETE_ICON);
        delete.addActionListener(e -> AttributeWidget.this.remove(true));
        menuList.add(0, delete);
        return menuList;
    }

    @Override
    public void init() {
        this.getClassWidget().scanDuplicateAttributes(null, this.name);
        validateName(null, this.getName());
        addOpenSourceCodeAction();
        setAttributeTooltip();
        visualizeDataType();
    }

    protected void addOpenSourceCodeAction() {
        this.getImageWidget().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        this.getImageWidget().getActions().addAction(
                new OpenSourceCodeAction(
                        () -> this.getClassWidget().getFileObject(),
                        this.getBaseElementSpec(),
                        this.getModelerScene().getModelerFile()
                )
        );
    }
    
    @Override
    public void destroy() {
        this.getClassWidget().scanDuplicateAttributes(this.name, null);
    }
    
    public void validateName(String previousName,String name){
        if (JavaPersistenceQLKeywords.isKeyword(name)) {
            getSignalManager().fire(ERROR, AttributeValidator.ATTRIBUTE_NAME_WITH_JPQL_KEYWORD);
        } else {
            getSignalManager().clear(ERROR, AttributeValidator.ATTRIBUTE_NAME_WITH_JPQL_KEYWORD);
        }
        if(SourceVersion.isName(name)){
            getSignalManager().clear(ERROR, AttributeValidator.INVALID_ATTRIBUTE_NAME);
        } else {
            getSignalManager().fire(ERROR, AttributeValidator.INVALID_ATTRIBUTE_NAME);
        }
        this.getClassWidget().scanDuplicateAttributes(previousName, name);

    }
    @Override
    public void setName(String name) {
        String previousName = this.name;
        if (name != null && !name.trim().isEmpty()) {
            this.name = name.replaceAll("\\s+", "");
            if (this.getModelerScene().getModelerFile().isLoaded()) {
                getBaseElementSpec().setName(this.name);
                refractorReference(previousName, this.name);
            }
        }
       validateName(previousName,this.getName());
    }
    
    private void refractorReference(String previousName, String newName) {
        if (previousName == null) {
            return;
        }
        ModelerFile modelerFile = this.getModelerScene().getModelerFile();
        RequestProcessor.getDefault().post(() -> {
            try {

                String singularPreName = previousName;
                String pluralPreName = English.plural(singularPreName);
                String singularNewName = newName;
                String pluralNewName = English.plural(singularNewName);
       
                //Refractor NamedQuery, NamedNativeQuery
                if (this.getClassWidget().getBaseElementSpec() instanceof IdentifiableClass) {
                    for (NamedQuery obj : new CopyOnWriteArrayList<>(((IdentifiableClass) this.getClassWidget().getBaseElementSpec()).getNamedQuery())) {
                        obj.refractorName(singularPreName, singularNewName);
                        obj.refractorName(pluralPreName, pluralNewName);
                        obj.refractorQuery(singularPreName, singularNewName);
                        obj.refractorQuery(pluralPreName, pluralNewName);
                    }
                    for (NamedNativeQuery obj : new CopyOnWriteArrayList<>(((IdentifiableClass) this.getClassWidget().getBaseElementSpec()).getNamedNativeQuery())) {
                        obj.refractorName(singularPreName, singularNewName);
                        obj.refractorName(pluralPreName, pluralNewName);
                        obj.refractorQuery(singularPreName, singularNewName);
                        obj.refractorQuery(pluralPreName, pluralNewName);
                    }
                }

               
            } catch (Throwable t) {
                modelerFile.handleException(t);
            }
        });
    }


    @Override
    public void setLabel(String label) {
        if (label != null && !label.trim().isEmpty()) {
            this.setPinName(label.replaceAll("\\s+", ""));
        }
    }

    @Override
    public void createVisualPropertySet(ElementPropertySet elementPropertySet) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void setAttributeTooltip() {
        this.setToolTipText(this.getBaseElementSpec().getDataTypeLabel());
    }

    /**
     * @return the classWidget
     */
    public JavaClassWidget getClassWidget() {
        return (JavaClassWidget) this.getPNodeWidget();
    }

    public static PropertyVisibilityHandler getMapKeyVisibilityHandler(Attribute attribute){
        return () -> {
            if(attribute instanceof CollectionTypeHandler){
                String classname = ((CollectionTypeHandler)attribute).getCollectionType();
                    try {
                        return Map.class.isAssignableFrom(Class.forName(classname));
                    } catch (ClassNotFoundException ex) { }
            }
            return false;
        };
    }
    protected void createMapKeyPropertySet(ElementPropertySet set){
        Attribute attribute = this.getBaseElementSpec();
        if(!(attribute instanceof MapKeyHandler)){
            throw new IllegalStateException("BaseElementSpec does not implements MapKeyHandler");
        }
        MapKeyHandler mapKeyHandler = (MapKeyHandler)attribute;
        PropertyVisibilityHandler mapKeyVisibilityHandler = getMapKeyVisibilityHandler(attribute);
        set.put("ATTR_PROP", PropertiesHandler.getMapKeyProperty(this, mapKeyHandler, mapKeyVisibilityHandler));
        set.put("ATTR_PROP", PropertiesHandler.getFieldTypeProperty("mapKeyFieldType", "Map Key", "", true, this));
        set.put("COLLECTION_TABLE_PROP", PropertiesHandler.getJoinColumnsProperty("mapKeyJoinColumns", "MapKey Join Columns", "", this.getModelerScene(), mapKeyHandler.getMapKeyJoinColumn()));
        
        
        this.addPropertyChangeListener("mapKeyType",(oldValue, value) -> {
            mapKeyHandler.resetMapAttribute(); 
            AttributeValidator.scanMapKeyHandlerError(this);
            visualizeDataType();
        });
        this.addPropertyVisibilityHandler("mapKeyType", mapKeyVisibilityHandler);
        this.addPropertyVisibilityHandler("mapKeyFieldType", () -> mapKeyVisibilityHandler.isVisible() && mapKeyHandler.getMapKeyType() == MapKeyType.NEW);
        this.addPropertyVisibilityHandler("mapKey", () -> mapKeyVisibilityHandler.isVisible() && mapKeyHandler.getMapKeyType() == MapKeyType.EXT);
        this.addPropertyVisibilityHandler("mapKeyJoinColumns", () -> {
               return mapKeyVisibilityHandler.isVisible() && mapKeyHandler.getMapKeyType() == MapKeyType.NEW && mapKeyHandler.getMapKeyEntity()!=null;
                        }
        );
    }

}
