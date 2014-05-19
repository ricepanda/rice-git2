/**
 * Copyright 2005-2014 The Kuali Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ecl2.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.rice.krad.uif.layout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.kuali.rice.krad.datadictionary.parse.BeanTag;
import org.kuali.rice.krad.datadictionary.parse.BeanTagAttribute;
import org.kuali.rice.krad.datadictionary.validator.ValidationTrace;
import org.kuali.rice.krad.uif.CssConstants;
import org.kuali.rice.krad.uif.UifConstants;
import org.kuali.rice.krad.uif.UifPropertyPaths;
import org.kuali.rice.krad.uif.component.Component;
import org.kuali.rice.krad.uif.component.DataBinding;
import org.kuali.rice.krad.uif.component.KeepExpression;
import org.kuali.rice.krad.uif.container.CollectionGroup;
import org.kuali.rice.krad.uif.container.Container;
import org.kuali.rice.krad.uif.container.Group;
import org.kuali.rice.krad.uif.container.collections.LineBuilderContext;
import org.kuali.rice.krad.uif.element.Action;
import org.kuali.rice.krad.uif.element.Label;
import org.kuali.rice.krad.uif.element.Message;
import org.kuali.rice.krad.uif.field.DataField;
import org.kuali.rice.krad.uif.field.Field;
import org.kuali.rice.krad.uif.field.FieldGroup;
import org.kuali.rice.krad.uif.field.InputField;
import org.kuali.rice.krad.uif.field.MessageField;
import org.kuali.rice.krad.uif.layout.collections.CollectionPagingHelper;
import org.kuali.rice.krad.uif.layout.collections.DataTablesPagingHelper;
import org.kuali.rice.krad.uif.lifecycle.ViewLifecycle;
import org.kuali.rice.krad.uif.lifecycle.ViewLifecycleRestriction;
import org.kuali.rice.krad.uif.util.ColumnCalculationInfo;
import org.kuali.rice.krad.uif.util.ComponentFactory;
import org.kuali.rice.krad.uif.util.ComponentUtils;
import org.kuali.rice.krad.uif.util.CopyUtils;
import org.kuali.rice.krad.uif.util.LifecycleElement;
import org.kuali.rice.krad.uif.view.ExpressionEvaluator;
import org.kuali.rice.krad.uif.view.View;
import org.kuali.rice.krad.uif.view.ViewModel;
import org.kuali.rice.krad.uif.widget.Pager;
import org.kuali.rice.krad.uif.widget.RichTable;
import org.kuali.rice.krad.util.KRADUtils;
import org.kuali.rice.krad.web.form.UifFormBase;

/**
 * Implementation of table layout manager.
 *
 * <p>Based on the fields defined, the {@code TableLayoutManager} will dynamically create instances of
 * the fields for each collection row. In addition, the manager can create standard fields like the
 * action and sequence fields for each row. The manager supports options inherited from the
 * {@code GridLayoutManager} such as rowSpan, colSpan, and cell width settings.</p>
 *
 * {@inheritDoc}
 *
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
@BeanTag(name = "tableCollectionLayout-bean", parent = "Uif-TableCollectionLayout")
public class TableLayoutManagerBase extends GridLayoutManagerBase implements TableLayoutManager {
    private static final long serialVersionUID = 3622267585541524208L;

    private boolean useShortLabels;
    private boolean repeatHeader;
    private Label headerLabelPrototype;

    private boolean renderSequenceField;
    private boolean generateAutoSequence;
    private Field sequenceFieldPrototype;

    private FieldGroup actionFieldPrototype;
    private FieldGroup subCollectionFieldGroupPrototype;
    private Field selectFieldPrototype;

    private boolean separateAddLine;
    private Group addLineGroup;

    // internal counter for the data columns (not including sequence, action)
    private int numberOfDataColumns;

    private List<Label> headerLabels;
    private List<Field> allRowFields;
    private List<Field> firstRowFields;

    private Pager pagerWidget;
    private RichTable richTable;
    private boolean headerAdded;

    private int actionColumnIndex = -1;
    private String actionColumnPlacement;

    //row details properties
    private Group rowDetailsGroup;
    private boolean rowDetailsOpen;
    private boolean showToggleAllDetails;
    private Action toggleAllDetailsAction;
    private boolean ajaxDetailsRetrieval;
    private Action expandDetailsActionPrototype;

    //grouping properties
    @KeepExpression
    private String groupingTitle;
    private String groupingPrefix;
    private int groupingColumnIndex;
    private List<String> groupingPropertyNames;

    //total properties
    private boolean renderOnlyLeftTotalLabels;
    private boolean showTotal;
    private boolean showPageTotal;
    private boolean showGroupTotal;
    private boolean generateGroupTotalRows;
    private Label totalLabel;
    private Label pageTotalLabel;
    private Label groupTotalLabelPrototype;

    private List<String> columnsToCalculate;
    private List<ColumnCalculationInfo> columnCalculations;
    private List<Component> footerCalculationComponents;

    //row css
    private Map<String, String> conditionalRowCssClasses;

    public TableLayoutManagerBase() {
        useShortLabels = false;
        repeatHeader = false;
        renderSequenceField = true;
        generateAutoSequence = false;
        separateAddLine = false;
        rowDetailsOpen = false;

        headerLabels = new ArrayList<Label>();
        allRowFields = new ArrayList<Field>();
        firstRowFields = new ArrayList<Field>();
        columnsToCalculate = new ArrayList<String>();
        columnCalculations = new ArrayList<ColumnCalculationInfo>();
        conditionalRowCssClasses = new HashMap<String, String>();
    }

    /**
     * The following actions are performed:
     *
     * <ul>
     * <li>Sets sequence field prototype if auto sequence is true</li>
     * <li>Initializes the prototypes</li>
     * </ul>
     *
     * {@inheritDoc}
     */
    @Override
    public void performInitialization(Object model) {
        CollectionGroup collectionGroup = (CollectionGroup) ViewLifecycle.getPhase().getElement();

        if (collectionGroup.isReadOnly()) {
            addLineGroup.setReadOnly(true);
            actionFieldPrototype.setReadOnly(true);
        }

        this.setupDetails(collectionGroup);
        this.setupGrouping(model, collectionGroup);

        if (collectionGroup.isAddViaLightBox()) {
            setSeparateAddLine(true);
        }

        super.performInitialization(model);

        getRowCssClasses().clear();

        if (generateAutoSequence && !(getSequenceFieldPrototype() instanceof MessageField)) {
            sequenceFieldPrototype = ComponentFactory.getMessageField();
        }
    }

    /**
     * performApplyModel override. Takes expressions that may be set in the columnCalculation
     * objects and populates them correctly into those component's propertyExpressions.
     *
     * @param view view instance to which the layout manager belongs
     * @param model Top level object containing the data (could be the form or a top level business
     * object, dto)
     * @param container
     *
     * {@inheritDoc}
     */
    @Override
    public void performApplyModel(Object model, LifecycleElement parent) {
        super.performApplyModel(model, parent);

        for (ColumnCalculationInfo cInfo : columnCalculations) {
            ViewLifecycle.getExpressionEvaluator().populatePropertyExpressionsFromGraph(cInfo, false);
        }
    }

    /**
     * Sets up the final column count for rendering based on whether the sequence and action fields
     * have been generated, sets up column calculations, and richTable rowGrouping options
     *
     * {@inheritDoc}
     */
    @Override
    public void performFinalize(Object model, LifecycleElement parent) {
        super.performFinalize(model, parent);

        UifFormBase formBase = (UifFormBase) model;

        CollectionGroup collectionGroup = (CollectionGroup) ViewLifecycle.getPhase().getElement();

        int totalColumns = getNumberOfDataColumns();
        if (renderSequenceField) {
            totalColumns++;
        }

        if (collectionGroup.isIncludeLineSelectionField()) {
            totalColumns++;
        }

        if (collectionGroup.isRenderLineActions() && !collectionGroup.isReadOnly()) {
            totalColumns++;
        }

        setNumberOfColumns(totalColumns);

        // Default equal cell widths class
        if (this.isApplyDefaultCellWidths()){
            this.addStyleClass("uif-table-fixed");
        }

        // if add line event, add highlighting for added row
        if (UifConstants.ActionEvents.ADD_LINE.equals(formBase.getActionEvent())) {
            String highlightScript = "jQuery(\"#" + parent.getId() + " tr:first\").effect(\"highlight\",{}, 6000);";
            String onReadyScript = collectionGroup.getOnDocumentReadyScript();
            if (StringUtils.isNotBlank(onReadyScript)) {
                highlightScript = onReadyScript + highlightScript;
            }
            collectionGroup.setOnDocumentReadyScript(highlightScript);
        }

        //setup the column calculations functionality and components
        if (columnCalculations != null && !columnCalculations.isEmpty() && richTable != null &&
                this.getAllRowFields() != null && !this.getAllRowFields().isEmpty()) {
            setupColumnCalculations(model, collectionGroup, totalColumns);
        }

        //set the js properties for rowGrouping on richTables
        if ((groupingPropertyNames != null || StringUtils.isNotBlank(this.getGroupingTitle())) && richTable != null) {
            richTable.setGroupingOptionsJSString("{iGroupingColumnIndex: "
                    + groupingColumnIndex
                    + ", bGenerateGroupTotalRows:"
                    + this.generateGroupTotalRows
                    + ", bSetGroupingClassOnTR: true"
                    + ", sGroupingClass: 'uif-groupRow'"
                    + (this.getGroupingPrefix() != null ? ", sGroupLabelPrefix: '" + this.getGroupingPrefix() + "'" :
                    "")
                    + "}");
        }

        // Calculate the number of pages for the pager widget if we are using server paging
        if ((this.getRichTable() == null || !this.getRichTable().isRender()) &&
                collectionGroup.isUseServerPaging() && this.getPagerWidget() != null) {
            // Set the appropriate page, total pages, and link script into the Pager
            CollectionLayoutUtils.setupPagerWidget(pagerWidget, collectionGroup, model);
        }

        // Add toggle all details action data in applicable
        if (toggleAllDetailsAction != null) {
            toggleAllDetailsAction.addDataAttribute("open", Boolean.toString(this.rowDetailsOpen));
            toggleAllDetailsAction.addDataAttribute("tableid", this.getId());
        }
    }

    /**
     * Sets up the grouping MessageField to be used in the first column of the table layout for
     * grouping collection content into groups based on values of the line's fields.
     *
     * @param model The model for the active lifecycle
     * @param collectionGroup collection group for this layout
     */
    protected void setupGrouping(Object model, CollectionGroup collectionGroup) {
        String groupingTitleExpression = "";

        if (StringUtils.isNotBlank(this.getPropertyExpression(UifPropertyPaths.GROUPING_TITLE))) {
            groupingTitleExpression = this.getPropertyExpression(UifPropertyPaths.GROUPING_TITLE);

            this.setGroupingTitle(this.getPropertyExpression(UifPropertyPaths.GROUPING_TITLE));
        } else if (this.getGroupingPropertyNames() != null) {
            for (String propertyName : this.getGroupingPropertyNames()) {
                groupingTitleExpression = groupingTitleExpression + ", " + propertyName;
            }

            groupingTitleExpression = groupingTitleExpression.replaceFirst(", ",
                    "@{" + UifConstants.LINE_PATH_BIND_ADJUST_PREFIX);
            groupingTitleExpression = groupingTitleExpression.replace(", ",
                    "}, @{" + UifConstants.LINE_PATH_BIND_ADJUST_PREFIX);
            groupingTitleExpression = groupingTitleExpression.trim() + "}";
        }

        if (StringUtils.isNotBlank(groupingTitleExpression)) {
            MessageField groupingMessageField = ComponentFactory.getColGroupingField();

            groupingMessageField.getMessage().getPropertyExpressions().put(UifPropertyPaths.MESSAGE_TEXT,
                    groupingTitleExpression);

            groupingMessageField.addDataAttribute(UifConstants.DataAttributes.ROLE,
                    UifConstants.RoleTypes.ROW_GROUPING);

            List<Component> theItems = new ArrayList<Component>();
            theItems.add(groupingMessageField);
            theItems.addAll(collectionGroup.getItems());
            collectionGroup.setItems(theItems);
        }
    }

    /**
     * Setup the column calculations functionality and components
     *
     * @param model the model
     * @param container the parent container
     * @param totalColumns total number of columns in the table
     */
    protected void setupColumnCalculations(Object model, Container container, int totalColumns) {
        footerCalculationComponents = new ArrayList<Component>(totalColumns);

        //add nulls for each column to start - nulls will be processed by the ftl as a blank cell
        for (int i = 0; i < totalColumns; i++) {
            footerCalculationComponents.add(null);
        }

        int leftLabelColumnIndex = 0;
        if (groupingPropertyNames != null || StringUtils.isNotBlank(this.getGroupingTitle())) {
            leftLabelColumnIndex = 1;
        }

        //process each column calculation
        for (ColumnCalculationInfo cInfo : columnCalculations) {
            //propertyName is REQUIRED throws exception if not set
            if (StringUtils.isNotBlank(cInfo.getPropertyName())) {
                for (int i = 0; i < this.getNumberOfColumns(); i++) {
                    Component component = this.getAllRowFields().get(i);
                    if (component != null && component instanceof DataField &&
                            ((DataField) component).getPropertyName().equals(cInfo.getPropertyName())) {
                        cInfo.setColumnNumber(i);
                    }
                }

                this.getColumnsToCalculate().add(cInfo.getColumnNumber().toString());
            } else {
                throw new RuntimeException("TableLayoutManager(" + container.getId() + "->" + this.getId() +
                        ") ColumnCalculationInfo MUST have a propertyName set");
            }

            // create a new field group to hold the totals fields
            FieldGroup calculationFieldGroup = ComponentFactory.getFieldGroup();
            calculationFieldGroup.addDataAttribute(UifConstants.DataAttributes.ROLE,
                    UifConstants.RoleTypes.TOTALS_BLOCK);

            List<Component> calculationFieldGroupItems = new ArrayList<Component>();

            //setup page total field and add it to footer's group for this column
            if (cInfo.isShowPageTotal()) {
                Field pageTotalDataField = CopyUtils.copy(cInfo.getPageTotalField());
                setupTotalField(pageTotalDataField, cInfo, this.isShowPageTotal(), getPageTotalLabel(),
                        UifConstants.RoleTypes.PAGE_TOTAL, leftLabelColumnIndex);
                calculationFieldGroupItems.add(pageTotalDataField);
            }

            //setup total field and add it to footer's group for this column
            if (cInfo.isShowTotal()) {
                Field totalDataField = CopyUtils.copy(cInfo.getTotalField());
                setupTotalField(totalDataField, cInfo, this.isShowTotal(), getTotalLabel(),
                        UifConstants.RoleTypes.TOTAL, leftLabelColumnIndex);

                if (!cInfo.isRecalculateTotalClientSide()) {
                    totalDataField.addDataAttribute(UifConstants.DataAttributes.SKIP_TOTAL, "true");
                }

                calculationFieldGroupItems.add(totalDataField);
            }

            //setup total field and add it to footer's group for this column
            //do not generate group total rows if group totals are not being shown
            if (cInfo.isShowGroupTotal()) {
                Field groupTotalDataField = CopyUtils.copy(cInfo.getGroupTotalFieldPrototype());
                setupTotalField(groupTotalDataField, cInfo, this.isShowGroupTotal(), getGroupTotalLabelPrototype(),
                        UifConstants.RoleTypes.GROUP_TOTAL, leftLabelColumnIndex);
                groupTotalDataField.setId(container.getId() + "_gTotal" + cInfo.getColumnNumber());
                groupTotalDataField.setStyle("display: none;");

                calculationFieldGroupItems.add(groupTotalDataField);

                if (this.isRenderOnlyLeftTotalLabels() && !this.isShowGroupTotal()) {
                    generateGroupTotalRows = false;
                } else {
                    generateGroupTotalRows = true;
                }
            }

            calculationFieldGroup.setItems(calculationFieldGroupItems);

            //Determine if there is already a fieldGroup present for this column's footer
            //if so create a new group and add the new calculation fields to the already existing ones
            //otherwise just add it
            Component component = footerCalculationComponents.get(cInfo.getColumnNumber());
            if (component != null && component instanceof FieldGroup) {
                Group verticalComboCalcGroup = ComponentFactory.getVerticalBoxGroup();

                List<Component> comboGroupItems = new ArrayList<Component>();
                comboGroupItems.add(component);
                comboGroupItems.add(calculationFieldGroup);
                verticalComboCalcGroup.setItems(comboGroupItems);

                footerCalculationComponents.set(cInfo.getColumnNumber(), verticalComboCalcGroup);
            } else if (component != null && component instanceof Group) {
                List<Component> comboGroupItems = new ArrayList<Component>();
                comboGroupItems.addAll(((Group) component).getItems());
                comboGroupItems.add(calculationFieldGroup);

                ((Group) component).setItems(comboGroupItems);

                footerCalculationComponents.set(cInfo.getColumnNumber(), component);
            } else {
                footerCalculationComponents.set(cInfo.getColumnNumber(), calculationFieldGroup);
            }
        }

        //special processing for the left labels - when there are no total fields in this column
        //add the label to the column footer directly
        if (this.renderOnlyLeftTotalLabels && footerCalculationComponents.get(leftLabelColumnIndex) == null) {

            List<Component> groupItems = new ArrayList<Component>();
            Group labelGroup = ComponentFactory.getVerticalBoxGroup();

            if (this.isShowGroupTotal()) {
                //display none - this label is copied by the javascript
                Label groupTotalLabel = CopyUtils.copy(groupTotalLabelPrototype);
                groupTotalLabel.setViewStatus(UifConstants.ViewStatus.CREATED);
                groupTotalLabel.setStyle("display: none;");
                groupTotalLabel.addDataAttribute(UifConstants.DataAttributes.ROLE, "groupTotalLabel");
                groupItems.add(groupTotalLabel);
            }

            if (this.isShowPageTotal()) {
                Label pageTotalLabel = CopyUtils.copy(this.pageTotalLabel);
                pageTotalLabel.setViewStatus(UifConstants.ViewStatus.CREATED);
                pageTotalLabel.addDataAttribute(UifConstants.DataAttributes.ROLE, "pageTotal");
                groupItems.add(pageTotalLabel);
            }

            if (this.isShowTotal()) {
                Label totalLabel = CopyUtils.copy(this.totalLabel);
                totalLabel.setViewStatus(UifConstants.ViewStatus.CREATED);
                groupItems.add(totalLabel);
            }

            labelGroup.setItems(groupItems);

            footerCalculationComponents.set(leftLabelColumnIndex, labelGroup);
        }
    }

    /**
     * Setup the totalField with the columnCalculationInfo(cInfo) passed in. Param show represents
     * the tableLayoutManager's setting for the type of total being processed.
     *
     * @param totalField the field to setup
     * @param cInfo ColumnCalculation info to use to setup the field
     * @param show show the field (if renderOnlyLeftTotalLabels is true, otherwise uses value in
     * cInfo)
     * @param leftLabel the leftLabel, not used if renderOnlyLeftTotalLabels is false
     * @param type type used to set the dataAttribute role - used by the js for selection
     * @param leftLabelColumnIndex index of the leftLabelColumn (0 or 1 if grouping enabled - hidden
     * column)
     * @return the field with cInfo and tableLayoutManager settings applied as appropriate
     */
    protected Field setupTotalField(Field totalField, ColumnCalculationInfo cInfo, boolean show, Label leftLabel,
            String type, int leftLabelColumnIndex) {
        //setup the totals field
        Field totalDataField = totalField;
        totalDataField.addDataAttribute(UifConstants.DataAttributes.ROLE, type);
        totalDataField.addDataAttribute("function", cInfo.getCalculationFunctionName());
        totalDataField.addDataAttribute("params", cInfo.getCalculationFunctionExtraData());

        if (cInfo.getColumnNumber() != leftLabelColumnIndex) {
            //do not render labels for columns which have totals and the renderOnlyLeftTotalLabels
            //flag is set
            totalDataField.getFieldLabel().setRender(!this.isRenderOnlyLeftTotalLabels());
        } else if (cInfo.getColumnNumber() == leftLabelColumnIndex && this.isRenderOnlyLeftTotalLabels()) {
            //renderOnlyLeftTotalLabel is set to true, but the column has a total itself - set the layout
            //manager settings directly into the field
            totalDataField.setFieldLabel((Label) CopyUtils.copy(leftLabel));
        }

        if (this.isRenderOnlyLeftTotalLabels()) {
            totalDataField.setRender(show);
        }

        return totalDataField;
    }

    /**
     * Assembles the field instances for the collection line.
     *
     * <p>The given sequence field prototype is copied for the line sequence field. Likewise a copy of
     * the actionFieldPrototype is made and the given actions are set as the items for the action field.
     * Finally the generated items are assembled together into the allRowFields list with the given
     * lineFields.</p>
     *
     * {@inheritDoc}
     */
    @Override
    public void buildLine(LineBuilderContext lineBuilderContext) {
        View view = ViewLifecycle.getView();

        List<Field> lineFields = lineBuilderContext.getLineFields();
        CollectionGroup collectionGroup = lineBuilderContext.getCollectionGroup();
        int lineIndex = lineBuilderContext.getLineIndex();
        String idSuffix = lineBuilderContext.getIdSuffix();
        Object currentLine = lineBuilderContext.getCurrentLine();
        List<? extends Component> actions = lineBuilderContext.getLineActions();
        String bindingPath = lineBuilderContext.getBindingPath();

        // since expressions are not evaluated on child components yet, we need to evaluate any properties
        // we are going to read for building the table
        ExpressionEvaluator expressionEvaluator = ViewLifecycle.getExpressionEvaluator();
        for (Field lineField : lineFields) {
            lineField.pushObjectToContext(UifConstants.ContextVariableNames.PARENT, collectionGroup);
            lineField.pushAllToContext(view.getContext());
            lineField.pushObjectToContext(UifConstants.ContextVariableNames.THEME_IMAGES,
                    view.getTheme().getImageDirectory());
            lineField.pushObjectToContext(UifConstants.ContextVariableNames.COMPONENT, lineField);

            expressionEvaluator.evaluatePropertyExpression(view, lineField.getContext(), lineField,
                    UifPropertyPaths.ROW_SPAN, true);
            expressionEvaluator.evaluatePropertyExpression(view, lineField.getContext(), lineField,
                    UifPropertyPaths.COL_SPAN, true);
            expressionEvaluator.evaluatePropertyExpression(view, lineField.getContext(), lineField,
                    UifPropertyPaths.REQUIRED, true);
            expressionEvaluator.evaluatePropertyExpression(view, lineField.getContext(), lineField,
                    UifPropertyPaths.READ_ONLY, true);
        }

        // if first line for table set number of data columns
        if (allRowFields.isEmpty()) {
            if (isSuppressLineWrapping()) {
                setNumberOfDataColumns(lineFields.size());
            } else {
                setNumberOfDataColumns(getNumberOfColumns());
            }
        }

        boolean isAddLine = false;

        // If first row or row wrap is happening
        if (lineIndex == -1 || (lineFields.size() != numberOfDataColumns
                && ((lineIndex + 1) * numberOfDataColumns) < lineFields.size())) {
            isAddLine = true;
        }

        // capture the first row of fields for widgets that build off the table
        if (lineIndex == 0 || this.firstRowFields.isEmpty()) {
            this.firstRowFields = lineFields;
        }

        boolean renderActions = collectionGroup.isRenderLineActions() && !collectionGroup.isReadOnly();
        int extraColumns = 0;
        String rowCss = "";
        boolean addLineInTable =
                collectionGroup.isRenderAddLine() && !collectionGroup.isReadOnly() && !isSeparateAddLine();

        if (collectionGroup.isHighlightNewItems() && ((UifFormBase) lineBuilderContext.getModel())
                .isAddedCollectionItem(currentLine)) {
            rowCss = collectionGroup.getNewItemsCssClass();
        } else if (isAddLine && addLineInTable) {
            rowCss = collectionGroup.getAddItemCssClass();
            this.addStyleClass(CssConstants.Classes.HAS_ADD_LINE);
        }

        // do not allow null rowCss
        if (rowCss == null) {
            rowCss = "";
        }

        // conditionalRowCssClass generation logic, if applicable
        if (conditionalRowCssClasses != null && !conditionalRowCssClasses.isEmpty()) {
            int oddRemainder = 1;
            if (!addLineInTable) {
                oddRemainder = 0;
            }

            boolean isOdd = lineIndex % 2 == oddRemainder || lineIndex == -1;
            Map<String, Object> lineContext = new HashMap<String, Object>();

            lineContext.putAll(this.getContext());
            lineContext.put(UifConstants.ContextVariableNames.LINE, currentLine);
            lineContext.put(UifConstants.ContextVariableNames.MANAGER, this);
            lineContext.put(UifConstants.ContextVariableNames.VIEW, view);
            lineContext.put(UifConstants.ContextVariableNames.LINE_SUFFIX, idSuffix);
            lineContext.put(UifConstants.ContextVariableNames.INDEX, Integer.valueOf(lineIndex));
            lineContext.put(UifConstants.ContextVariableNames.COLLECTION_GROUP, collectionGroup);
            lineContext.put(UifConstants.ContextVariableNames.IS_ADD_LINE, isAddLine && !isSeparateAddLine());
            lineContext.put(UifConstants.ContextVariableNames.READONLY_LINE, collectionGroup.isReadOnly());

            // get row css based on conditionalRowCssClasses map
            rowCss = rowCss + " " + KRADUtils.generateRowCssClassString(conditionalRowCssClasses, lineIndex, isOdd,
                    lineContext, expressionEvaluator);
        }

        // if separate add line prepare the add line group
        if (isAddLine && separateAddLine) {
            if (StringUtils.isBlank(addLineGroup.getTitle()) && StringUtils.isBlank(
                    addLineGroup.getHeader().getHeaderText())) {
                addLineGroup.getHeader().setHeaderText(collectionGroup.getAddLabel());
            }
            addLineGroup.setItems(lineFields);

            List<Component> footerItems = new ArrayList<Component>(actions);
            footerItems.addAll(addLineGroup.getFooter().getItems());
            addLineGroup.getFooter().setItems(footerItems);

            if (collectionGroup.isAddViaLightBox()) {
                String actionScript = "showLightboxComponent('" + addLineGroup.getId() + "');";
                if (StringUtils.isNotBlank(collectionGroup.getAddViaLightBoxAction().getActionScript())) {
                    actionScript = collectionGroup.getAddViaLightBoxAction().getActionScript() + actionScript;
                }
                collectionGroup.getAddViaLightBoxAction().setActionScript(actionScript);
                addLineGroup.setStyle("display: none");
            }

            // Note that a RowCssClass was not added to the LayoutManager for the collection for the separateAddLine
            return;
        }

        rowCss = StringUtils.removeStart(rowCss, " ");
        this.getRowCssClasses().add(rowCss);

        // TODO: implement repeat header
        if (!headerAdded) {
            headerLabels = new ArrayList<Label>();
            allRowFields = new ArrayList<Field>();

            buildTableHeaderRows(collectionGroup, lineFields);
            ComponentUtils.pushObjectToContext(headerLabels, UifConstants.ContextVariableNames.LINE, currentLine);
            ComponentUtils.pushObjectToContext(headerLabels, UifConstants.ContextVariableNames.INDEX, new Integer(
                    lineIndex));
            headerAdded = true;
        }

        // set label field rendered to true on line fields and adjust cell properties
        for (Field field : lineFields) {
            field.setLabelRendered(true);
            field.setFieldLabel(null);

            setCellAttributes(field);
        }

        int rowCount = calculateNumberOfRows(lineFields);
        int rowSpan = rowCount;

        List<FieldGroup> subCollectionFields = lineBuilderContext.getSubCollectionFields();
        if (subCollectionFields != null) {
            rowSpan += subCollectionFields.size();
        }

        if (actionColumnIndex == 1 && renderActions) {
            addActionColumn(collectionGroup, idSuffix, currentLine, lineIndex, rowSpan, actions);
        }

        // sequence field is always first and should span all rows for the line
        if (renderSequenceField) {
            Field sequenceField = null;
            if (!isAddLine) {
                sequenceField = ComponentUtils.copy(getSequenceFieldPrototype(), idSuffix);

                //Ignore in validation processing
                sequenceField.addDataAttribute(UifConstants.DataAttributes.VIGNORE, "yes");

                if (generateAutoSequence && (sequenceField instanceof MessageField)) {
                    ((MessageField) sequenceField).setMessageText(Integer.toString(lineIndex + 1));
                }
            } else {
                sequenceField = ComponentFactory.getMessageField();

                Message sequenceMessage = ComponentUtils.copy(collectionGroup.getAddLineLabel(), idSuffix);
                ((MessageField) sequenceField).setMessage(sequenceMessage);

                // adjusting add line label to match sequence prototype cells attributes
                sequenceField.setCellWidth(getSequenceFieldPrototype().getCellWidth());
                sequenceField.setWrapperStyle(getSequenceFieldPrototype().getWrapperStyle());
            }

            sequenceField.setRowSpan(rowSpan);

            if (sequenceField instanceof DataBinding) {
                ((DataBinding) sequenceField).getBindingInfo().setBindByNamePrefix(bindingPath);
            }

            setCellAttributes(sequenceField);

            ComponentUtils.updateContextForLine(sequenceField, collectionGroup, currentLine, lineIndex, idSuffix);
            allRowFields.add(sequenceField);
            
            extraColumns++;

            if (actionColumnIndex == 2 && renderActions) {
                addActionColumn(collectionGroup, idSuffix, currentLine, lineIndex, rowSpan, actions);
            }
        }

        // select field will come after sequence field (if enabled) or be first column
        if (collectionGroup.isIncludeLineSelectionField()) {
            Field selectField = ComponentUtils.copy(getSelectFieldPrototype(), idSuffix);
            CollectionLayoutUtils.prepareSelectFieldForLine(selectField, collectionGroup, bindingPath, currentLine);

            ComponentUtils.updateContextForLine(selectField, collectionGroup, currentLine, lineIndex, idSuffix);
            setCellAttributes(selectField);

            allRowFields.add(selectField);

            extraColumns++;

            if (renderActions) {
                if ((actionColumnIndex == 3 && renderSequenceField) || (actionColumnIndex == 2
                        && !renderSequenceField)) {
                    addActionColumn(collectionGroup, idSuffix, currentLine, lineIndex, rowSpan, actions);
                }
            }
        }

        // now add the fields in the correct position
        int cellPosition = 0;

        boolean renderActionsLast = actionColumnIndex == -1 || actionColumnIndex > lineFields.size() + extraColumns;
        boolean hasGrouping = (groupingPropertyNames != null || StringUtils.isNotBlank(this.getGroupingTitle()));
        boolean insertActionField = false;

        for (Field lineField : lineFields) {
            //Check to see if ActionField needs to be inserted before this lineField because of wrapping.
            // Since actionField has a colSpan of 1 add that to the previous cellPosition instead of the
            // current lineField's colSpan.
            // Only insert if ActionField has to be placed at the end. Else the specification of actionColumnIndex should
            // take care of putting it in the right location
            insertActionField = (cellPosition != 0 && lineFields.size() != numberOfDataColumns)
                    && renderActions
                    && renderActionsLast
                    && ((cellPosition % numberOfDataColumns) == 0);

            cellPosition += lineField.getColSpan();

            //special handling for grouping field - this field MUST be first
            Map<String, String> lineFieldDataAttributes = lineField.getDataAttributes();
            if (hasGrouping && (lineField instanceof MessageField) &&
                    lineFieldDataAttributes != null && UifConstants.RoleTypes.ROW_GROUPING.equals(
                    lineFieldDataAttributes.get(UifConstants.DataAttributes.ROLE))) {
                int groupFieldIndex = allRowFields.size() - extraColumns;
                allRowFields.add(groupFieldIndex, lineField);
                groupingColumnIndex = 0;
                if (isAddLine) {
                    ((MessageField) lineField).getMessage().getPropertyExpressions().remove(
                            UifPropertyPaths.MESSAGE_TEXT);
                    ((MessageField) lineField).getMessage().setMessageText("addLine");
                }
            } else {
                // If the row wraps before the last element
                if (insertActionField) {
                    addActionColumn(collectionGroup, idSuffix, currentLine, lineIndex, rowSpan, actions);
                }

                allRowFields.add(lineField);
            }

            // action field
            if (!renderActionsLast && cellPosition == (actionColumnIndex - extraColumns - 1)) {
                addActionColumn(collectionGroup, idSuffix, currentLine, lineIndex, rowSpan, actions);
            }

            //details action
            if (lineField instanceof FieldGroup && ((FieldGroup) lineField).getItems() != null) {
                for (Component component : ((FieldGroup) lineField).getItems()) {
                    if (component != null
                            && component instanceof Action
                            && (component.getDataAttributes() != null)
                            && component.getDataAttributes().get("role") != null
                            && component.getDataAttributes().get("role").equals("detailsLink")
                            && StringUtils.isBlank(((Action) component).getActionScript())) {
                        ((Action) component).setActionScript("rowDetailsActionHandler(this,'" + this.getId() + "');");
                    }
                }
            }

            //special column calculation handling to identify what type of handler will be attached
            //and add special styling
            if (lineField instanceof InputField && columnCalculations != null) {
                for (ColumnCalculationInfo cInfo : columnCalculations) {
                    if (cInfo.getPropertyName().equals(((InputField) lineField).getPropertyName())) {
                        if (cInfo.isCalculateOnKeyUp()) {
                            lineField.addDataAttribute(UifConstants.DataAttributes.TOTAL, "keyup");
                        } else {
                            lineField.addDataAttribute(UifConstants.DataAttributes.TOTAL, "change");
                        }
                        lineField.addStyleClass("uif-calculationField");
                    }
                }
            }
        }

        if (lineFields.size() == numberOfDataColumns && renderActions && renderActionsLast) {
            addActionColumn(collectionGroup, idSuffix, currentLine, lineIndex, rowSpan, actions);
        }

        // update colspan on sub-collection fields
        if (subCollectionFields != null) {
            for (FieldGroup subCollectionField : subCollectionFields) {
                subCollectionField.setColSpan(numberOfDataColumns);
            }

            // add sub-collection fields to end of data fields
            allRowFields.addAll(subCollectionFields);
        }
    }

    /**
     * Creates a field group wrapper for the given actions based on
     * {@link TableLayoutManagerBase#getActionFieldPrototype()}.
     *
     * @param collectionGroup collection group being built
     * @param idSuffix id suffix for the action field
     * @param currentLine line object for the current line being built
     * @param lineIndex index of the line being built
     * @param rowSpan number of rows the action field should span
     * @param actions action components that should be to the field group
     */
    protected void addActionColumn(CollectionGroup collectionGroup, String idSuffix, Object currentLine, int lineIndex,
            int rowSpan, List<? extends Component> actions) {
        FieldGroup lineActionsField = ComponentUtils.copy(getActionFieldPrototype(), idSuffix);

        ComponentUtils.updateContextForLine(lineActionsField, collectionGroup, currentLine, lineIndex, idSuffix);

        lineActionsField.setRowSpan(rowSpan);
        lineActionsField.setItems(actions);
        if (lineActionsField.getWrapperCssClasses() != null && !lineActionsField.getWrapperCssClasses().contains(
                CssConstants.Classes.ACTION_COLUMN_STYLE_CLASS)) {
            lineActionsField.getWrapperCssClasses().add(CssConstants.Classes.ACTION_COLUMN_STYLE_CLASS);
        } else {
            lineActionsField.setWrapperCssClasses(Arrays.asList(CssConstants.Classes.ACTION_COLUMN_STYLE_CLASS));
        }

        setCellAttributes(lineActionsField);

        allRowFields.add(lineActionsField);
    }

    /**
     * Create the {@code Label} instances that will be used to render the table header
     *
     * <p>
     * For each column, a copy of headerLabelPrototype is made that determines the label
     * configuration. The actual label text comes from the field for which the header applies to.
     * The first column is always the sequence (if enabled) and the last column contains the
     * actions. Both the sequence and action header fields will span all rows for the header.
     * </p>
     *
     * <p>
     * The headerLabels list will contain the final list of header fields built
     * </p>
     *
     * @param collectionGroup CollectionGroup container the table applies to
     * @param lineFields fields for the data columns from which the headers are pulled
     */
    protected void buildTableHeaderRows(CollectionGroup collectionGroup, List<Field> lineFields) {
        // row count needed to determine the row span for the sequence and
        // action fields, since they should span all rows for the line
        int rowCount = calculateNumberOfRows(lineFields);

        boolean renderActions = collectionGroup.isRenderLineActions() && !collectionGroup.isReadOnly();

        String idSuffix = collectionGroup.getSubCollectionSuffix();

        int extraColumns = 0;

        if (actionColumnIndex == 1 && renderActions) {
            addActionHeader(rowCount, idSuffix, 1);
        }

        // first column is sequence label (if action column not 1)
        if (renderSequenceField) {
            getSequenceFieldPrototype().setLabelRendered(true);
            getSequenceFieldPrototype().setRowSpan(rowCount);
            addHeaderField(getSequenceFieldPrototype(), idSuffix, 1);
            extraColumns++;

            if (actionColumnIndex == 2 && renderActions) {
                addActionHeader(rowCount, idSuffix, 2);
            }
        }

        // next is select field
        if (collectionGroup.isIncludeLineSelectionField()) {
            getSelectFieldPrototype().setLabelRendered(true);
            getSelectFieldPrototype().setRowSpan(rowCount);
            addHeaderField(getSelectFieldPrototype(), idSuffix, 1);
            extraColumns++;

            if (actionColumnIndex == 3 && renderActions && renderSequenceField) {
                addActionHeader(rowCount, idSuffix, 3);
            } else if (actionColumnIndex == 2 && renderActions) {
                addActionHeader(rowCount, idSuffix, 2);
            }
        }

        // pull out label fields from the container's items
        int cellPosition = 0;
        boolean renderActionsLast = actionColumnIndex == -1 || actionColumnIndex > lineFields.size() + extraColumns;
        boolean insertActionHeader = false;
        for (Field field : lineFields) {
            if (!field.isRender() && StringUtils.isEmpty(field.getProgressiveRender())) {
                continue;
            }

            //Check to see if ActionField needs to be inserted before this lineField because of wrapping.
            // Since actionField has a colSpan of 1 add that to the previous cellPosition instead of the
            // current lineField's colSpan.
            // Only Insert if ActionField has to be placed at the end. Else the specification of actionColumnIndex
            // should take care of putting it in the right location
            insertActionHeader = (cellPosition != 0
                    && lineFields.size() != numberOfDataColumns
                    && renderActions
                    && renderActionsLast
                    && ((cellPosition % numberOfDataColumns) == 0));

            if (insertActionHeader) {
                addActionHeader(rowCount, idSuffix, cellPosition);
            }

            cellPosition += field.getColSpan();
            addHeaderField(field, idSuffix, cellPosition);

            // add action header
            if (renderActions && !renderActionsLast && cellPosition == actionColumnIndex - extraColumns - 1) {
                cellPosition += 1;
                addActionHeader(rowCount, idSuffix, cellPosition);
            }
        }

        if (lineFields.size() == numberOfDataColumns && renderActions && renderActionsLast) {
            cellPosition += 1;
            addActionHeader(rowCount, idSuffix, cellPosition);
        }
    }

    /**
     * Adds the action header
     *
     * @param rowCount
     * @param idSuffix suffix for the header id, also column will be added
     * @param cellPosition
     */
    protected void addActionHeader(int rowCount, String idSuffix, int cellPosition) {
        getActionFieldPrototype().setLabelRendered(true);
        getActionFieldPrototype().setRowSpan(rowCount);
        if (getActionFieldPrototype().getWrapperCssClasses() != null && !getActionFieldPrototype()
                .getWrapperCssClasses().contains(CssConstants.Classes.ACTION_COLUMN_STYLE_CLASS)) {
            getActionFieldPrototype().getWrapperCssClasses().add(CssConstants.Classes.ACTION_COLUMN_STYLE_CLASS);
        } else {
            getActionFieldPrototype().setWrapperCssClasses(Arrays.asList(
                    CssConstants.Classes.ACTION_COLUMN_STYLE_CLASS));
        }

        addHeaderField(getActionFieldPrototype(), idSuffix, cellPosition);
    }

    /**
     * Creates a new instance of the header field prototype and then sets the label to the short (if
     * useShortLabels is set to true) or long label of the given component. After created the header
     * field is added to the list making up the table header
     *
     * @param field field instance the header field is being created for
     * @param idSuffix suffix for the header id, also column will be added
     * @param column column number for the header, used for setting the id
     */
    protected void addHeaderField(Field field, String idSuffix, int column) {
        String labelSuffix = UifConstants.IdSuffixes.COLUMN + column;
        if (StringUtils.isNotBlank(idSuffix)) {
            labelSuffix = idSuffix + labelSuffix;
        }

        Label headerLabel = ComponentUtils.copy(getHeaderLabelPrototype(), labelSuffix);

        if (useShortLabels) {
            headerLabel.setLabelText(field.getShortLabel());
        } else {
            headerLabel.setLabelText(field.getLabel());
        }

        headerLabel.setInlineComponents(field.getFieldLabel().getInlineComponents());

        headerLabel.setRowSpan(field.getRowSpan());
        headerLabel.setColSpan(field.getColSpan());

        if ((field.getRequired() != null) && field.getRequired().booleanValue()) {
            headerLabel.setRenderRequiredIndicator(!field.isReadOnly());
        } else {
            headerLabel.setRenderRequiredIndicator(false);
        }

        setCellAttributes(field);

        // copy cell attributes from the field to the label
        headerLabel.setWrapperCssClasses(field.getWrapperCssClasses());
        headerLabel.setWrapperStyle(field.getWrapperStyle());
        headerLabel.setCellWidth(field.getCellWidth());

        headerLabels.add(headerLabel);
    }

    /**
     * Calculates how many rows will be needed per collection line to display the list of fields.
     * Assumption is made that the total number of cells the fields take up is evenly divisible by
     * the configured number of columns
     *
     * @param items list of items that make up one collection line
     * @return number of rows
     */
    protected int calculateNumberOfRows(List<? extends Field> items) {
        int rowCount = 0;

        // check flag that indicates only one row should be created
        if (isSuppressLineWrapping()) {
            return 1;
        }

        // If Overflow is greater than 0 then calculate the col span for the last item in the overflowed row
        if (items.size() % getNumberOfDataColumns() > 0) {
            //get the last line item
            Field field = items.get(items.size() - 1);

            int colSize = 0;
            for (Field f : items) {
                colSize += f.getColSpan();
            }

            field.setColSpan(1 + (numberOfDataColumns - (colSize % numberOfDataColumns)));
            rowCount = ((items.size() / getNumberOfDataColumns()) + 1);
        } else {
            rowCount = items.size() / getNumberOfDataColumns();
        }
        return rowCount;
    }

    /**
     * Invokes instance of {@link org.kuali.rice.krad.uif.layout.collections.DataTablesPagingHelper} to carry out
     * the paging request using data tables API.
     *
     * <p>There are two types of paging supported in the table layout, one that uses data tables paging API, and one
     * that handles basic table paging.</p>
     *
     * {@inheritDoc}
     */
    @Override
    public void processPagingRequest(Object model, CollectionGroup collectionGroup) {
        boolean richTableEnabled = ((getRichTable() != null) && (getRichTable().isRender()));

        if (richTableEnabled) {
            DataTablesPagingHelper.DataTablesInputs dataTablesInputs = new DataTablesPagingHelper.DataTablesInputs(
                    ViewLifecycle.getRequest());

            DataTablesPagingHelper.processPagingRequest(ViewLifecycle.getView(), (ViewModel) model, collectionGroup,
                    dataTablesInputs);
        } else {
            String pageNumber = ViewLifecycle.getRequest().getParameter(UifConstants.PageRequest.PAGE_NUMBER);

            CollectionPagingHelper pagingHelper = new CollectionPagingHelper();
            pagingHelper.processPagingRequest(ViewLifecycle.getView(), collectionGroup, (UifFormBase) model,
                    pageNumber);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<? extends Container> getSupportedContainer() {
        return CollectionGroup.class;
    }

    /**
     * Indicates whether the short label for the collection field should be used as the table header
     * or the regular label
     *
     * @return true if short label should be used, false if long label should be used
     */
    @Override
    public List<Component> getColumnCalculationComponents() {
        if (columnCalculations == null) {
            return Collections.emptyList();
        }
        
        List<Component> components = new ArrayList<Component>(columnCalculations.size() * 3);
        for (ColumnCalculationInfo cInfo : columnCalculations) {
            components.add(cInfo.getTotalField());
            components.add(cInfo.getPageTotalField());
            components.add(cInfo.getGroupTotalFieldPrototype());
        }
        return components;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @BeanTagAttribute(name = "useShortLabels")
    public boolean isUseShortLabels() {
        return this.useShortLabels;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setUseShortLabels(boolean useShortLabels) {
        this.useShortLabels = useShortLabels;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @BeanTagAttribute(name = "repeatHeader")
    public boolean isRepeatHeader() {
        return this.repeatHeader;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setRepeatHeader(boolean repeatHeader) {
        this.repeatHeader = repeatHeader;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ViewLifecycleRestriction(UifConstants.ViewPhases.INITIALIZE)
    @BeanTagAttribute(name = "headerLabelPrototype", type = BeanTagAttribute.AttributeType.SINGLEBEAN)
    public Label getHeaderLabelPrototype() {
        return this.headerLabelPrototype;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setHeaderLabelPrototype(Label headerLabelPrototype) {
        this.headerLabelPrototype = headerLabelPrototype;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Label> getHeaderLabels() {
        return this.headerLabels;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @BeanTagAttribute(name = "renderSequenceField")
    public boolean isRenderSequenceField() {
        return this.renderSequenceField;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setRenderSequenceField(boolean renderSequenceField) {
        this.renderSequenceField = renderSequenceField;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @BeanTagAttribute(name = "sequencePropertyName")
    public String getSequencePropertyName() {
        if ((getSequenceFieldPrototype() != null) && (getSequenceFieldPrototype() instanceof DataField)) {
            return ((DataField) getSequenceFieldPrototype()).getPropertyName();
        }

        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSequencePropertyName(String sequencePropertyName) {
        if ((getSequenceFieldPrototype() != null) && (getSequenceFieldPrototype() instanceof DataField)) {
            ((DataField) getSequenceFieldPrototype()).setPropertyName(sequencePropertyName);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @BeanTagAttribute(name = "generateAutoSequence")
    public boolean isGenerateAutoSequence() {
        return this.generateAutoSequence;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setGenerateAutoSequence(boolean generateAutoSequence) {
        this.generateAutoSequence = generateAutoSequence;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ViewLifecycleRestriction(UifConstants.ViewPhases.INITIALIZE)
    @BeanTagAttribute(name = "sequenceFieldPrototype", type = BeanTagAttribute.AttributeType.SINGLEBEAN)
    public Field getSequenceFieldPrototype() {
        return this.sequenceFieldPrototype;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSequenceFieldPrototype(Field sequenceFieldPrototype) {
        this.sequenceFieldPrototype = sequenceFieldPrototype;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ViewLifecycleRestriction(UifConstants.ViewPhases.INITIALIZE)
    @BeanTagAttribute(name = "actionFieldPrototype", type = BeanTagAttribute.AttributeType.SINGLEBEAN)
    public FieldGroup getActionFieldPrototype() {
        return this.actionFieldPrototype;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setActionFieldPrototype(FieldGroup actionFieldPrototype) {
        this.actionFieldPrototype = actionFieldPrototype;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ViewLifecycleRestriction(UifConstants.ViewPhases.INITIALIZE)
    @BeanTagAttribute(name = "subCollectionFieldGroupPrototype", type = BeanTagAttribute.AttributeType.SINGLEBEAN)
    public FieldGroup getSubCollectionFieldGroupPrototype() {
        return this.subCollectionFieldGroupPrototype;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSubCollectionFieldGroupPrototype(FieldGroup subCollectionFieldGroupPrototype) {
        this.subCollectionFieldGroupPrototype = subCollectionFieldGroupPrototype;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ViewLifecycleRestriction(UifConstants.ViewPhases.INITIALIZE)
    @BeanTagAttribute(name = "selectFieldPrototype", type = BeanTagAttribute.AttributeType.SINGLEBEAN)
    public Field getSelectFieldPrototype() {
        return selectFieldPrototype;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSelectFieldPrototype(Field selectFieldPrototype) {
        this.selectFieldPrototype = selectFieldPrototype;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @BeanTagAttribute(name = "separateAddLine")
    public boolean isSeparateAddLine() {
        return separateAddLine;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSeparateAddLine(boolean separateAddLine) {
        this.separateAddLine = separateAddLine;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @BeanTagAttribute(name = "addLineGroup", type = BeanTagAttribute.AttributeType.SINGLEBEAN)
    public Group getAddLineGroup() {
        return addLineGroup;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAddLineGroup(Group addLineGroup) {
        this.addLineGroup = addLineGroup;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Field> getAllRowFields() {
        return this.allRowFields;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ViewLifecycleRestriction
    public List<Field> getFirstRowFields() {
        return firstRowFields;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Pager getPagerWidget() {
        return pagerWidget;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPagerWidget(Pager pagerWidget) {
        this.pagerWidget = pagerWidget;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @BeanTagAttribute(name = "richTable", type = BeanTagAttribute.AttributeType.SINGLEBEAN)
    public RichTable getRichTable() {
        return this.richTable;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setRichTable(RichTable richTable) {
        this.richTable = richTable;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @BeanTagAttribute(name = "numberOfDataColumns")
    public int getNumberOfDataColumns() {
        return this.numberOfDataColumns;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setNumberOfDataColumns(int numberOfDataColumns) {
        this.numberOfDataColumns = numberOfDataColumns;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @BeanTagAttribute(name = "hiddenColumns", type = BeanTagAttribute.AttributeType.SETVALUE)
    public Set<String> getHiddenColumns() {
        if (richTable != null) {
            return richTable.getHiddenColumns();
        }

        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setHiddenColumns(Set<String> hiddenColumns) {
        if (richTable != null) {
            richTable.setHiddenColumns(hiddenColumns);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @BeanTagAttribute(name = "sortableColumns", type = BeanTagAttribute.AttributeType.SETVALUE)
    public Set<String> getSortableColumns() {
        if (richTable != null) {
            return richTable.getSortableColumns();
        }

        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSortableColumns(Set<String> sortableColumns) {
        if (richTable != null) {
            richTable.setSortableColumns(sortableColumns);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @BeanTagAttribute(name = "actionColumnIndex")
    public int getActionColumnIndex() {
        return actionColumnIndex;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @BeanTagAttribute(name = "actionColumnPlacement")
    public String getActionColumnPlacement() {
        return actionColumnPlacement;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setActionColumnPlacement(String actionColumnPlacement) {
        this.actionColumnPlacement = actionColumnPlacement;

        if ("LEFT".equals(actionColumnPlacement)) {
            actionColumnIndex = 1;
        } else if ("RIGHT".equals(actionColumnPlacement)) {
            actionColumnIndex = -1;
        } else if (StringUtils.isNumeric(actionColumnPlacement)) {
            actionColumnIndex = Integer.parseInt(actionColumnPlacement);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ViewLifecycleRestriction(UifConstants.ViewPhases.PRE_PROCESS)
    @BeanTagAttribute(name = "rowDetailsGroup", type = BeanTagAttribute.AttributeType.SINGLEBEAN)
    public Group getRowDetailsGroup() {
        return rowDetailsGroup;
    }

    /**
     * @see TableLayoutManagerBase#getRowDetailsGroup()
     */
    @Override
    public void setRowDetailsGroup(Group rowDetailsGroup) {
        this.rowDetailsGroup = rowDetailsGroup;
    }

    /**
     * Creates the details group for the line using the information setup through the setter methods
     * of this interface. Line details are currently only supported in TableLayoutManagers which use
     * richTable.
     *
     * @param collectionGroup the CollectionGroup for this TableLayoutManager
     */
    public void setupDetails(CollectionGroup collectionGroup) {
        if (getRowDetailsGroup() == null || this.getRichTable() == null || !this.getRichTable().isRender()) {
            return;
        }

        //data attribute to mark this group to open itself when rendered
        collectionGroup.addDataAttribute(UifConstants.DataAttributes.DETAILS_DEFAULT_OPEN, Boolean.toString(
                this.rowDetailsOpen));

        FieldGroup detailsFieldGroup = ComponentFactory.getFieldGroup();

        TreeMap<String, String> dataAttributes = new TreeMap<String, String>();
        dataAttributes.put(UifConstants.DataAttributes.ROLE, "detailsFieldGroup");
        detailsFieldGroup.setDataAttributes(dataAttributes);

        Action rowDetailsAction = this.getExpandDetailsActionPrototype();
        rowDetailsAction.addDataAttribute(UifConstants.DataAttributes.ROLE, "detailsLink");
        rowDetailsAction.setId(collectionGroup.getId() + UifConstants.IdSuffixes.DETAIL_LINK);

        List<Component> detailsItems = new ArrayList<Component>();
        detailsItems.add(rowDetailsAction);

        dataAttributes = new TreeMap<String, String>();
        dataAttributes.put("role", "details");
        dataAttributes.put("open", Boolean.toString(this.rowDetailsOpen));
        this.getRowDetailsGroup().setDataAttributes(dataAttributes);

        detailsItems.add(getRowDetailsGroup());
        detailsFieldGroup.setItems(detailsItems);
        detailsFieldGroup.setId(collectionGroup.getId() + UifConstants.IdSuffixes.DETAIL_GROUP);

        if (ajaxDetailsRetrieval && !this.rowDetailsOpen) {
            this.getRowDetailsGroup().setRetrieveViaAjax(true);
        } else {
            this.getRowDetailsGroup().setHidden(true);
        }
        
        detailsFieldGroup.setReadOnly(collectionGroup.isReadOnly());

        List<Component> theItems = new ArrayList<Component>();
        theItems.add(detailsFieldGroup);
        theItems.addAll(collectionGroup.getItems());

        collectionGroup.setItems(theItems);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getColumnsToCalculate() {
        return columnsToCalculate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @BeanTagAttribute(name = "showTotal")
    public boolean isShowTotal() {
        return showTotal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setShowTotal(boolean showTotal) {
        this.showTotal = showTotal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @BeanTagAttribute(name = "showPageTotal")
    public boolean isShowPageTotal() {
        return showPageTotal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setShowPageTotal(boolean showPageTotal) {
        this.showPageTotal = showPageTotal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @BeanTagAttribute(name = "showGroupTotal")
    public boolean isShowGroupTotal() {
        return showGroupTotal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setShowGroupTotal(boolean showGroupTotal) {
        this.showGroupTotal = showGroupTotal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @BeanTagAttribute(name = "totalLabel", type = BeanTagAttribute.AttributeType.SINGLEBEAN)
    public Label getTotalLabel() {
        return totalLabel;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTotalLabel(Label totalLabel) {
        this.totalLabel = totalLabel;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @BeanTagAttribute(name = "pageTotalLabel", type = BeanTagAttribute.AttributeType.SINGLEBEAN)
    public Label getPageTotalLabel() {
        return pageTotalLabel;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPageTotalLabel(Label pageTotalLabel) {
        this.pageTotalLabel = pageTotalLabel;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @BeanTagAttribute(name = "groupTotalLabelPrototype", type = BeanTagAttribute.AttributeType.SINGLEBEAN)
    public Label getGroupTotalLabelPrototype() {
        return groupTotalLabelPrototype;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setGroupTotalLabelPrototype(Label groupTotalLabelPrototype) {
        this.groupTotalLabelPrototype = groupTotalLabelPrototype;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @BeanTagAttribute(name = "columnCalculations", type = BeanTagAttribute.AttributeType.LISTBEAN)
    public List<ColumnCalculationInfo> getColumnCalculations() {
        return columnCalculations;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setColumnCalculations(List<ColumnCalculationInfo> columnCalculations) {
        this.columnCalculations = columnCalculations;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @BeanTagAttribute(name = "renderOnlyLeftTotalLabels")
    public boolean isRenderOnlyLeftTotalLabels() {
        return renderOnlyLeftTotalLabels;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setRenderOnlyLeftTotalLabels(boolean renderOnlyLeftTotalLabels) {
        this.renderOnlyLeftTotalLabels = renderOnlyLeftTotalLabels;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Component> getFooterCalculationComponents() {
        return footerCalculationComponents;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @BeanTagAttribute(name = "groupingPropertyNames", type = BeanTagAttribute.AttributeType.LISTVALUE)
    public List<String> getGroupingPropertyNames() {
        return groupingPropertyNames;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setGroupingPropertyNames(List<String> groupingPropertyNames) {
        this.groupingPropertyNames = groupingPropertyNames;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @BeanTagAttribute(name = "groupingTitle")
    public String getGroupingTitle() {
        return groupingTitle;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setGroupingTitle(String groupingTitle) {
        if (groupingTitle != null && !groupingTitle.contains("@{")) {
            throw new RuntimeException("groupingTitle MUST contain a springEL expression to uniquely"
                    + " identify a collection group (often related to some value of the line). "
                    + "Value provided: "
                    + this.getGroupingTitle());
        }
        this.groupingTitle = groupingTitle;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @BeanTagAttribute(name = "groupingPrefix")
    public String getGroupingPrefix() {
        return groupingPrefix;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setGroupingPrefix(String groupingPrefix) {
        this.groupingPrefix = groupingPrefix;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isRowDetailsOpen() {
        return rowDetailsOpen;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setRowDetailsOpen(boolean rowDetailsOpen) {
        this.rowDetailsOpen = rowDetailsOpen;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isShowToggleAllDetails() {
        return showToggleAllDetails;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setShowToggleAllDetails(boolean showToggleAllDetails) {
        this.showToggleAllDetails = showToggleAllDetails;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Action getToggleAllDetailsAction() {
        return toggleAllDetailsAction;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setToggleAllDetailsAction(Action toggleAllDetailsAction) {
        this.toggleAllDetailsAction = toggleAllDetailsAction;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAjaxDetailsRetrieval() {
        return ajaxDetailsRetrieval;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAjaxDetailsRetrieval(boolean ajaxDetailsRetrieval) {
        this.ajaxDetailsRetrieval = ajaxDetailsRetrieval;
    }

    /**
     * {@inheritDoc}
     */
    @ViewLifecycleRestriction(UifConstants.ViewPhases.INITIALIZE)
    @Override
    public Action getExpandDetailsActionPrototype() {
        return expandDetailsActionPrototype;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getGroupingColumnIndex() {
        return groupingColumnIndex;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setExpandDetailsActionPrototype(Action expandDetailsActionPrototype) {
        this.expandDetailsActionPrototype = expandDetailsActionPrototype;
    }

    /**
     * Set the header labels
     *
     * @param headerLabels
     */
    protected void setHeaderLabels(List<Label> headerLabels) {
        this.headerLabels = headerLabels;
    }

    /**
     * Set the row fields
     *
     * @param allRowFields
     */
    protected void setAllRowFields(List<Field> allRowFields) {
        this.allRowFields = allRowFields;
    }

    /**
     * Set the first row fields
     *
     * @param firstRowFields
     */
    protected void setFirstRowFields(List<Field> firstRowFields) {
        this.firstRowFields = firstRowFields;
    }

    /**
     * Set flag of whether a header is added
     *
     * @param headerAdded
     */
    protected void setHeaderAdded(boolean headerAdded) {
        this.headerAdded = headerAdded;
    }

    /**
     * Sets action column index
     *
     * @param actionColumnIndex
     */
    protected void setActionColumnIndex(int actionColumnIndex) {
        this.actionColumnIndex = actionColumnIndex;
    }

    /**
     * Set grouping column index
     *
     * @param groupingColumnIndex
     */
    protected void setGroupingColumnIndex(int groupingColumnIndex) {
        this.groupingColumnIndex = groupingColumnIndex;
    }

    /**
     * Set flag generate group total rows
     *
     * @param generateGroupTotalRows
     */
    protected void setGenerateGroupTotalRows(boolean generateGroupTotalRows) {
        this.generateGroupTotalRows = generateGroupTotalRows;
    }

    /**
     * Set columns to calculate
     *
     * @param columnsToCalculate
     */
    protected void setColumnsToCalculate(List<String> columnsToCalculate) {
        this.columnsToCalculate = columnsToCalculate;
    }

    /**
     * Set footer calculation components
     *
     * @param footerCalculationComponents
     */
    protected void setFooterCalculationComponents(List<Component> footerCalculationComponents) {
        this.footerCalculationComponents = footerCalculationComponents;
    }

    /**
     * The row css classes for the rows of this layout
     *
     * <p>
     * To set a css class on all rows, use "all" as a key. To set a class for even rows, use "even"
     * as a key, for odd rows, use "odd". Use a one-based index to target a specific row by index.
     * SpringEL can be used as a key and the expression will be evaluated; if evaluated to true, the
     * class(es) specified will be applied.
     * </p>
     *
     * @return a map which represents the css classes of the rows of this layout
     */
    @BeanTagAttribute(name = "conditionalRowCssClasses", type = BeanTagAttribute.AttributeType.MAPVALUE)
    public Map<String, String> getConditionalRowCssClasses() {
        return conditionalRowCssClasses;
    }

    /**
     * Set the conditionalRowCssClasses
     *
     * @param conditionalRowCssClasses
     */
    public void setConditionalRowCssClasses(Map<String, String> conditionalRowCssClasses) {
        this.conditionalRowCssClasses = conditionalRowCssClasses;
    }

    /**
     * Validates different requirements of component compiling a series of reports detailing
     * information on errors found in the component. Used by the RiceDictionaryValidator.
     *
     * @param tracer record of component's location
     */
    public void completeValidation(ValidationTrace tracer) {
        tracer.addBean("TableLayoutManager", getId());

        if (getRowDetailsGroup() != null) {
            boolean validTable = false;
            if (getRichTable() != null) {
                if (getRichTable().isRender()) {
                    validTable = true;
                }
            }
            if (!validTable) {
                String currentValues[] = {"rowDetailsGroup =" + getRowDetailsGroup(), "richTable =" + getRichTable()};
                tracer.createError("If rowDetailsGroup is set richTable must be set and its render true",
                        currentValues);
            }

        }
    }

}
