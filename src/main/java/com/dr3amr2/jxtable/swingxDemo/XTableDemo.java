/*
 * Copyright 2007-2008 Sun Microsystems, Inc.  All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Sun Microsystems nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.dr3amr2.jxtable.swingxDemo;

import com.dr3amr2.jxtable.utils.Stacker;
import com.dr3amr2.jxtable.utils.CustomColumnFactory;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.BindingGroup;
import org.jdesktop.beansbinding.Bindings;
import org.jdesktop.swingx.JXFrame;
import org.jdesktop.swingx.JXStatusBar;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.JXTableHeader;
import org.jdesktop.swingx.decorator.HighlighterFactory;

import javax.swing.*;
import javax.swing.SwingWorker.StateValue;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ;
    
public class XTableDemo extends JPanel {
    static final Logger logger = Logger.getLogger(XTableDemo.class.getName());

    private OscarTableModel oscarModel;

    private JPanel controlPanel;
    private Stacker dataPanel;
    private JXTable oscarTable;
    private JCheckBox winnersCheckbox;
    private JTextField filterField;
    private JComponent statusBarLeft;
    private JLabel actionStatus;
    private JLabel tableStatus;
    private JLabel tableRows;

    private JProgressBar progressBar;

    private OscarFiltering filterController;

    public XTableDemo() {
        initComponents();
        configureDisplayProperties();
        bind();
    }

    /**
     * Customizes display properties of contained components.
     * This is data-unrelated.
     */
    private void configureDisplayProperties() {
        //JXTable display properties
        // show column control
        oscarTable.setColumnControlVisible(true);
        // replace grid lines with striping 
        oscarTable.setShowGrid(false, false);
        oscarTable.addHighlighter(HighlighterFactory.createSimpleStriping());
        // initialize preferred size for table's viewable area
        oscarTable.setVisibleRowCount(10);

        //JXTable column properties
        // create and configure a custom column factory
        CustomColumnFactory factory = new CustomColumnFactory();
        OscarRendering.configureColumnFactory(factory, getClass());
        // set the factory before setting the table model
        oscarTable.setColumnFactory(factory);
    }

    /**
     * Binds components to data and user interaction.
     */
    protected void bind() {
        
        //JXTable data properties
        oscarModel = new OscarTableModel();
        // set the table model after setting the factory
        oscarTable.setModel(oscarModel);

        // Filter control
        //      create the controller
        filterController = new OscarFiltering(oscarTable);
        // bind controller properties to input components
        BindingGroup filterGroup = new BindingGroup();
        filterGroup.addBinding(Bindings.createAutoBinding(READ,
                winnersCheckbox, BeanProperty.create("selected"),
                filterController, BeanProperty.create("showOnlyWinners")));
        filterGroup.addBinding(Bindings.createAutoBinding(READ,
                filterField, BeanProperty.create("text"),
                filterController, BeanProperty.create("filterString")));
        // PENDING JW: crude hack to update the statusbar - fake property
        // how-to do cleanly?
        filterGroup.addBinding(Bindings.createAutoBinding(READ,
                filterController, BeanProperty.create("showOnlyWinners"),
                this, BeanProperty.create("statusContent")));
        filterGroup.addBinding(Bindings.createAutoBinding(READ,
                filterController, BeanProperty.create("filterString"),
                this, BeanProperty.create("statusContent")));
        filterGroup.bind();

        oscarModel.addTableModelListener(new TableModelListener() {
            public void tableChanged(TableModelEvent e) {
                updateStatusBar();
            }
        });

        //  JXTable column properties
        //      some display properties can be configured only after the model has been set, here:
        //      configure the view sequence of columns to be different from the model
        oscarTable.setColumnSequence(new Object[] {
                OscarTableModel.year_ID,
                OscarTableModel.category_ID,
                OscarTableModel.movieTitle_ID,
                OscarTableModel.nominees_ID
        });
    }

    /**
     * Binding artefact method: crude hack to update the status bar on state changes
     * from the controller. 
     */
    public void setStatusContent(Object dummy) {
        updateStatusBar();
    }

    /**
     * Updates status labels. Called during loading and on 
     * changes to the filter controller state.
     */
    protected void updateStatusBar() {
        tableStatus.setName(filterController.isFilteringByString() 
                ? "searchCountLabel" : "rowCountLabel");
        tableRows.setText("" + oscarTable.getRowCount());
    }
    
    /**
     * Callback method for demo loader. 
     */
    public void start() {
        if (oscarModel.getRowCount() != 0) return;
        //  Use SwingWorker to asynchronously load the data
        //      create SwingWorker which will load the data on a separate thread
        SwingWorker<?, ?> loader = new OscarDataLoader(
                XTableDemo.class.getResource("/demo/oscars.xml"), oscarModel);
        
        // display progress bar while data loads
        progressBar = new JProgressBar();
        statusBarLeft.add(progressBar);
        // bind the worker's progress notification to the progressBar
        // and the worker's state notification to this
        BindingGroup group = new BindingGroup();
        group.addBinding(Bindings.createAutoBinding(READ,
                loader, BeanProperty.create("progress"),
                progressBar, BeanProperty.create("value")));
        group.addBinding(Bindings.createAutoBinding(READ,
                loader, BeanProperty.create("state"),
                this, BeanProperty.create("loadState")));
        group.bind();
        loader.execute();
    }

    /**
     * Callback for worker's state notification: cleanup if done.
     * @param state
     */
    public void setLoadState(StateValue state) {
        //  Use SwingWorker to asynchronously load the data
        //      remove progressbar if done loading
        if (state != StateValue.DONE) return;
        statusBarLeft.remove(progressBar);
        statusBarLeft.remove(actionStatus);
        revalidate();
        repaint();
    }
    
    //  Use SwingWorker to asynchronously load the data
    //      specialized on OscarCandidate
    private class OscarDataLoader extends SwingWorker<List<OscarCandidate>, OscarCandidate> {
        private final URL oscarData;
        private final OscarTableModel oscarModel;
        private final List<OscarCandidate> candidates = new ArrayList<OscarCandidate>();
        private JLabel credits;
         
        private OscarDataLoader(URL oscarURL, OscarTableModel oscarTableModel) {
            this.oscarData = oscarURL;
            this.oscarModel = oscarTableModel;
        }

        //  Use SwingWorker to asynchronously load the data
        //      background task let a parser do its stuff and
        //      update a progress bar
        @Override
        public List<OscarCandidate> doInBackground() {
            OscarDataParser parser = new OscarDataParser() {
                @Override
                protected void addCandidate(OscarCandidate candidate) {
                    candidates.add(candidate);
                    if (candidates.size() % 3 == 0) {
                        try { // slow it down so we can see progress :-)
                            Thread.sleep(1);
                        } catch (Exception ex) {
                        }
                    }
                    publish(candidate);
                    setProgress(100 * candidates.size() / 8545);
                }
            };
            parser.parseDocument(oscarData);
            return candidates;
        }

        @Override
        protected void process(List<OscarCandidate> moreCandidates) {
            if (credits == null) {
                showCredits();
            }
            oscarModel.add(moreCandidates);
        }

        // For older Java 6 on OS X
        @SuppressWarnings("unused")
        protected void process(OscarCandidate... moreCandidates) {
            for (OscarCandidate candidate : moreCandidates) {
                oscarModel.add(candidate);
            }
        }
        
        //  Use SwingWorker to asynchronously load the data
        //      show a transparent overlay on start loading
        private void showCredits() {
            credits = new JLabel(); 
            credits.setName("credits");
            credits.setFont(UIManager.getFont("Table.font").deriveFont(24f));
            credits.setHorizontalAlignment(JLabel.CENTER);
            credits.setBorder(new CompoundBorder(new TitledBorder(""),
                    new EmptyBorder(20,20,20,20)));

            dataPanel.showMessageLayer(credits, .75f);
        }

        @Override
        //  Use SwingWorker to asynchronously load the data
        //      hide transparend overlay on end loading
        protected void done() {
            setProgress(100);
            dataPanel.hideMessageLayer();
        }
    }
    
//------------------ init ui    
    //  JXTable display properties
    //      center column header text
    private JXTable createXTable() {
        JXTable table = new JXTable() {

            @Override
            protected JTableHeader createDefaultTableHeader() {
                return new JXTableHeader(columnModel) {

                    @Override
                    public void updateUI() {
                        super.updateUI();
                        // need to do in updateUI to survive toggling of LAF
                        if (getDefaultRenderer() instanceof JLabel) {
                            ((JLabel) getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);
                            
                        }
                    }
                };
            }
            
        };
        return table;
    }
   
    protected void initComponents() {
        setLayout(new BorderLayout());

        controlPanel = createControlPanel();
        add(controlPanel, BorderLayout.NORTH);
        oscarTable = createXTable();
        oscarTable.setName("oscarTable");
        
        JScrollPane scrollpane = new JScrollPane(oscarTable);
        dataPanel = new Stacker(scrollpane);
        add(dataPanel, BorderLayout.CENTER);

        add(createStatusBar(), BorderLayout.SOUTH);
    }

    protected JPanel createControlPanel() {
        JPanel controlPanel = new JPanel();
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        controlPanel.setLayout(gridbag);

        c.gridx = 0;
        c.gridy = 1;
        c.gridheight = 1;
        c.insets = new Insets(20, 10, 0, 10);
        c.anchor = GridBagConstraints.SOUTHWEST;
        JLabel searchLabel = new JLabel();
        searchLabel.setName("searchLabel");
        controlPanel.add(searchLabel, c);

        c.gridx = 0;
        c.gridy = 2;
        c.weightx = 1.0;
        c.insets.top = 0;
        c.insets.bottom = 12;
        c.anchor = GridBagConstraints.SOUTHWEST;
        //c.fill = GridBagConstraints.HORIZONTAL;
        filterField = new JTextField(24);
        controlPanel.add(filterField, c);

        c.gridx = 1;
        c.gridy = 2;
        c.gridwidth = GridBagConstraints.REMAINDER;
        //c.insets.right = 24;
        //c.insets.left = 12;
        c.weightx = 0.0;
        c.anchor = GridBagConstraints.EAST;
        c.fill = GridBagConstraints.NONE;
        winnersCheckbox = new JCheckBox();
        winnersCheckbox.setName("winnersLabel");
        controlPanel.add(winnersCheckbox, c);

        return controlPanel;
    }

    protected Container createStatusBar() {

        JXStatusBar statusBar = new JXStatusBar();
        statusBar.putClientProperty("auto-add-separator", Boolean.FALSE);
        // Left status area
        statusBar.add(Box.createRigidArea(new Dimension(10, 22)));
        statusBarLeft = Box.createHorizontalBox();
        statusBar.add(statusBarLeft, JXStatusBar.Constraint.ResizeBehavior.FILL);
        actionStatus = new JLabel();
        actionStatus.setName("loadingStatusLabel");
        actionStatus.setHorizontalAlignment(JLabel.LEADING);
        statusBarLeft.add(actionStatus);

        // Middle (should stretch)
//        statusBar.add(Box.createHorizontalGlue());
//        statusBar.add(Box.createHorizontalGlue());
        statusBar.add(Box.createVerticalGlue());
        statusBar.add(Box.createRigidArea(new Dimension(50, 0)));

        // Right status area
        tableStatus = new JLabel(); 
        tableStatus.setName("rowCountLabel");
        JComponent bar = Box.createHorizontalBox();
        bar.add(tableStatus);
        tableRows = new JLabel("0");
        bar.add(tableRows);
        
        statusBar.add(bar);
        statusBar.add(Box.createHorizontalStrut(12));
        return statusBar;
    }

    public static void main(String args[]) {

        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JXFrame frame = new JXFrame("JXTable Demo", true);
                XTableDemo demo = new XTableDemo();
                frame.add(demo);
                frame.setSize(700, 400);
                frame.setVisible(true);
                demo.start();
            }
        });
    }
    
//-----do nothing methods (keep beansbinding happy)
    
    public Object getStatusContent() {
        return null;
    }
    
    public StateValue getLoadState() {
        return null;
    }

}