package com.dr3amr2.jxtable.ibsFilter; /**
* Created by dnguyen on 3/24/14.
*/
import org.jdesktop.beans.AbstractBean;
import org.jdesktop.swingx.JXTable;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;


public class DataFiltering extends AbstractBean {
    private RowFilter<TableModel, Integer> winnerFilter;
    private RowFilter<TableModel, Integer> searchFilter;

    private String filterString;
    private boolean showOnlyWinners = false;
    private JXTable filterTable;


    public DataFiltering(JXTable filterTable) {
        this.filterTable = filterTable;
    }

    public boolean isFilteringByString() {
        return !isEmpty(getFilterString());
    }

    private boolean isEmpty(String filterString) {
        return filterString == null || filterString.length() == 0;
    }

    /**
     * @param filterString the filterString to set
     */
    public void setFilterString(String filterString) {
        String oldValue = getFilterString();
        // <snip> Filter control
        // set the filter string (bound to the input in the textfield)
        // and update the search RowFilter
        this.filterString = filterString;
        updateSearchFilter();
        //        </snip>
        firePropertyChange("filterString", oldValue, getFilterString());
    }

    /**
     * @return the filterString
     */
    public String getFilterString() {
        return filterString;
    }

    /**
     * @param showOnlyWinners the showOnlyWinners to set
     */
    public void setShowOnlyWinners(boolean showOnlyWinners) {
        if (isShowOnlyWinners() == showOnlyWinners) return;
        boolean oldValue = isShowOnlyWinners();
        this.showOnlyWinners = showOnlyWinners;
        updateWinnerFilter();
        firePropertyChange("showOnlyWinners", oldValue, isShowOnlyWinners());
    }

    /**
     * @return the showOnlyWinners
     */
    public boolean isShowOnlyWinners() {
        return showOnlyWinners;
    }


    private void updateWinnerFilter() {
//        winnerFilter = showOnlyWinners ? createWinnerFilter() : null;
        updateFilters();
    }

    private void updateSearchFilter() {
        if ((filterString != null) && (filterString.length() > 0)) {
            searchFilter = createSearchFilter(filterString + ".*");
        } else {
            searchFilter = null;
        }
        updateFilters();
    }


    private void updateFilters() {
        // <snip> Filter control
        // set the filters to the table
        if ((searchFilter != null) && (winnerFilter != null)) {
            List<RowFilter<TableModel, Integer>> filters =
                    new ArrayList<RowFilter<TableModel, Integer>>(2);
            filters.add(winnerFilter);
            filters.add(searchFilter);
            RowFilter<TableModel, Integer> comboFilter = RowFilter.andFilter(filters);
            filterTable.setRowFilter(comboFilter);
        } else {
            if (searchFilter != null) {

                filterTable.setRowFilter(searchFilter);
            } else {
                filterTable.setRowFilter(winnerFilter);
            }
        }
        //        </snip>
    }


    private RowFilter<TableModel, Integer> createWinnerFilter() {
        return new RowFilter<TableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends TableModel, ? extends Integer> entry) {
                FilterTableModel oscarModel = (FilterTableModel) entry.getModel();
                FilterDataBean candidate = oscarModel.getCandidate(((Integer) entry.getIdentifier()).intValue());
                if (candidate.isFilterOn()) {
                    // Returning true indicates this row should be shown.
                    return true;
                }
                // loser
                return false;
            }
        };
    }

    //  Filter control
    //      create and return a custom RowFilter specialized on FilterDataBean
    private RowFilter<TableModel, Integer> createSearchFilter(final String filterString) {
        return new RowFilter<TableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends TableModel, ? extends Integer> entry) {
                FilterTableModel filterTableModel = (FilterTableModel) entry.getModel();
                FilterDataBean contact = filterTableModel.getCandidate((Integer) entry.getIdentifier());
                boolean matches = false;
                Pattern p = Pattern.compile(filterString + ".*", Pattern.CASE_INSENSITIVE);

                // Match against all columns
                String filterSearch = contact.getFilter();
                if (filterSearch != null) {
                    matches = p.matcher(filterSearch).matches();
                }

                String descriptionSearch = contact.getDescription();
                if (descriptionSearch != null && matches == false) {
                    matches = p.matcher(descriptionSearch).matches();
                }

                String nameSearch = contact.getName();
                if (nameSearch != null && matches == false) {
                    matches = p.matcher(nameSearch).matches();
                }

                String userSearch = contact.getUser();
                if (userSearch != null && matches == false) {
                    matches = p.matcher(userSearch).matches();
                }

                List<String> filters = contact.getFilters();
                for (String filter : filters) {
                    // match against persons as well
                    if (p.matcher(filter).matches()) {
                        matches = true;
                    }
                }
                return matches;
            }
        };
    }

}