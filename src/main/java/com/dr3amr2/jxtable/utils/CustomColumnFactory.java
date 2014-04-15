package com.dr3amr2.jxtable.utils;/*
 * Created on 29.02.2008
 *
 */

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.renderer.ComponentProvider;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import org.jdesktop.swingx.table.ColumnFactory;
import org.jdesktop.swingx.table.TableColumnExt;

import javax.swing.table.TableModel;
import java.util.*;
import java.util.logging.Logger;

//<snip> JXTable column properties

/**
 * A ColumnFactory is used by JXTable to create and configure all columns.
 * It can be set per-application (before creating an JXTable) or per-table
 * (before setting the model). <p>
 * 
 * Additional features:
 * <ul>
 * <li> reads column titles from Application context provided ResourceManager
 * <li> supports list of identifiers to exclude from column creation
 * <li> supports list of identifiers to hide on column configuration
 * <li> supports pluggable maximal rowCount to measure
 * <li> supports per-column componentProviders 
 * <li> supports per-column highlighters
 * <li> supports per-column sizing hints by prototypes
 * </ul>
 */
//</snip>
public  class CustomColumnFactory extends ColumnFactory {
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(CustomColumnFactory.class.getName());

    /**
     * {@inheritDoc} <p>
     * Overridden to not create columns for the model columns which are 
     * listed in the excludes.
     */
    @Override
    public TableColumnExt createAndConfigureTableColumn(TableModel model,
            int modelIndex) {
        if (getExcludeNames().contains(model.getColumnName(modelIndex))) {
            return null;
        }
        return super.createAndConfigureTableColumn(model, modelIndex);
    }

    /**
     * {@inheritDoc} <p>
     * 
     * Overridden to set the column's identifier, lookup the title 
     */
    @Override
    public void configureTableColumn(TableModel model,
            TableColumnExt columnExt) {
        super.configureTableColumn(model, columnExt);
        columnExt.setIdentifier(model.getColumnName(columnExt.getModelIndex()));
        configureTitle(columnExt);
        ComponentProvider<?> provider = getComponentProvider(columnExt.getIdentifier());
        if (provider != null) {
            columnExt.setCellRenderer(new DefaultTableRenderer(provider));
        }
        Highlighter highlighter = getHighlighter(columnExt.getIdentifier());
        if (highlighter != null) {
            columnExt.setHighlighters(highlighter);
        }
        columnExt.setComparator(getComparator(columnExt.getIdentifier()));
        columnExt.setPrototypeValue(getPrototypeValue(columnExt.getIdentifier()));
        if (getHiddenNames().contains(columnExt.getIdentifier())) {
            columnExt.setVisible(false);
        }
    }

    private void configureTitle(TableColumnExt columnExt) {
        //  columnExt.setTitle(DemoUtils.getResourceString(baseClass, columnExt.getIdentifier().toString()));
    }
    
    /**
     * {@inheritDoc} <p>
     * Overridden to check if the wants to limit the count of rows when
     * measuring. 
     */
    @Override
    protected int getRowCount(JXTable table) {
        if (table.getClientProperty("ColumnFactory.maxRowCount") instanceof Integer) {
            return Math.min(table.getRowCount(), 
                    (Integer) table.getClientProperty("ColumnFactory.maxRowCount"));
        }
        return super.getRowCount(table);
    }

//------------------------ manage the column properties (by identifier)    

    /**
     * 
     * @param names the identifiers to exclude.
     */
    public void addExcludeNames(Object... names) {
        for (Object object : names) {
            getExcludeNames().add(object);
        }
    }
    
    /**
     * 
     * @param names the identifiers to exclude.
     */
    public void addHiddenNames(Object... names) {
        for (Object object : names) {
            getHiddenNames().add(object);
        }
    }

    public void setBaseClass(Class<?> baseClass) {
        /* base class for resource lookup. */
        Class<?> baseClass1 = baseClass;
    }
    
    public void addComponentProvider(Object id, ComponentProvider<?> provider) {
        getComponentProviders().put(id, provider);
    }
    
    public void addPrototypeValue(Object id, Object prototype) {
        getPrototypeValues().put(id, prototype);
    }
    
    public void addHighlighter(Object id, Highlighter hl) {
        getHighlighters().put(id, hl);
    }

    public void addComparator(Object id, Comparator<?> comparator) {
        getComparators().put(id, comparator);
    }
 

    private List<Object> excludes;
    private List<Object> hidden;

    private Map<Object, ComponentProvider<?>> componentProviders;
    private Map<Object, Highlighter> highlighters;
    private Map<Object, Object> prototypes;
    private Map<Object, Comparator<?>> comparators;

    private List<Object> getExcludeNames() {
        if (excludes == null) {
            excludes = new ArrayList<>();
        }
        return excludes;
    }
    
    private List<Object> getHiddenNames() {
        if (hidden == null) {
            hidden = new ArrayList<>();
        }
        return hidden;
    }
    
    private Map<Object, ComponentProvider<?>> getComponentProviders() {
        if (componentProviders == null) {
            componentProviders = new HashMap<>();
        }
        return componentProviders;
    }
    
    private ComponentProvider<?> getComponentProvider(Object id) {
        return componentProviders != null ? componentProviders.get(id) : null;
    }
    
    private Map<Object, Object> getPrototypeValues() {
        if (prototypes == null) {
            prototypes = new HashMap<>();
        }
        return prototypes;
    }

    private Object getPrototypeValue(Object id) {
        return prototypes != null ? prototypes.get(id) : null;
    }
    
    private Map<Object, Highlighter> getHighlighters() {
        if (highlighters == null) {
            highlighters = new HashMap<>();
        }
        return highlighters;
    }

    private Highlighter getHighlighter(Object id) {
        return highlighters != null ? highlighters.get(id) : null;
    }
    
    private Map<Object, Comparator<?>> getComparators() {
        if (comparators == null) {
            comparators = new HashMap<>();
        }
        return comparators;
    }

    private Comparator<?> getComparator(Object id) {
        return getComparators().get(id);
    }
    

}

