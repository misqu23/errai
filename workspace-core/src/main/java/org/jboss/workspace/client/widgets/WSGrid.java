package org.jboss.workspace.client.widgets;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventPreview;
import com.google.gwt.user.client.ui.*;

import java.util.ArrayList;

public class WSGrid extends Composite {
    private VerticalPanel panel;
    private WSAbstractGrid titleBar;
    private WSAbstractGrid dataGrid;

    private ArrayList<Integer> columnWidths;

    private int cols;

    private WSCell currentFocus;
    private boolean currentFocusRowColSpan;
    private int _rsize = 0;
    private boolean _resizeArmed = false;
    private boolean _resizing = false;

    private WSGrid wsGrid = this;
    private PopupPanel resizeLine = new PopupPanel() {
        @Override
        public void onBrowserEvent(Event event) {
            wsGrid.onBrowserEvent(event);
        }
    };

    private ArrayList<Integer> colSizes = new ArrayList<Integer>();

    public WSGrid() {
        this(true);
    }

    public WSGrid(boolean scrollable) {
        panel = new VerticalPanel();

        initWidget(panel);

        titleBar = new WSAbstractGrid(false, GridType.TITLEBAR);

        panel.add(titleBar);

        titleBar.setStylePrimaryName("WSGrid-header");
        dataGrid = new WSAbstractGrid(scrollable, GridType.EDITABLE_GRID);

        panel.add(dataGrid);

        panel.setCellVerticalAlignment(dataGrid, HasVerticalAlignment.ALIGN_TOP);

        dataGrid.setStylePrimaryName("WSGrid-datagrid");

        columnWidths = new ArrayList<Integer>();

        resizeLine.setWidth("1px");
        resizeLine.setHeight("800px");
        resizeLine.setStyleName("WSGrid-resize-line");

        sinkEvents(Event.MOUSEEVENTS);
        resizeLine.sinkEvents(Event.MOUSEEVENTS);

        DOM.sinkEvents(RootPanel.getBodyElement(), Event.ONKEYDOWN |
                DOM.getEventsSunk(RootPanel.getBodyElement()));

        DOM.addEventPreview(new EventPreview() {
            public boolean onEventPreview(Event event) {

                switch (event.getTypeInt()) {
                    case Event.ONKEYPRESS:
                        if (currentFocus == null || currentFocus.edit) return true;

                        dataGrid.tableIndex.get(0).get(0).wrappedWidget.setHTML(event.getKeyCode() + "");

                        switch (event.getKeyCode()) {
                            case KeyboardListener.KEY_TAB:
                                if (event.getShiftKey()) {
                                    if (currentFocus.getCol() == 0 && currentFocus.getRow() > 0) {
                                        dataGrid.tableIndex.get(currentFocus.getRow() - 1).get(cols - 1).focus();
                                    }
                                    else {
                                        dataGrid.tableIndex.get(currentFocus.getRow()).get(currentFocus.getCol() - 1).focus();
                                    }
                                }
                                else {
                                    if (currentFocus.getCol() == cols - 1 && currentFocus.getRow() < dataGrid.tableIndex.size()) {
                                        dataGrid.tableIndex.get(currentFocus.getRow() + 1).get(0).focus();
                                    }
                                    else {
                                        dataGrid.tableIndex.get(currentFocus.getRow()).get(currentFocus.getCol() + 1).focus();
                                    }
                                }
                                break;

                            case 63232:
                            case KeyboardListener.KEY_UP:
                                if (currentFocus.getRow() > 0)
                                    dataGrid.tableIndex.get(currentFocus.getRow() - 1).get(currentFocus.getCol()).focus();
                                break;
                            case 63235:
                            case KeyboardListener.KEY_RIGHT:
                                if (currentFocus.getCol() < cols)
                                    dataGrid.tableIndex.get(currentFocus.getRow()).get(currentFocus.getCol() + 1).focus();
                                break;
                            case 63233:
                            case KeyboardListener.KEY_ENTER:
                            case KeyboardListener.KEY_DOWN:
                                if (currentFocus.getRow() < dataGrid.tableIndex.size())
                                    dataGrid.tableIndex.get(currentFocus.getRow() + 1).get(currentFocus.getCol()).focus();
                                break;
                            case 63234:
                            case KeyboardListener.KEY_LEFT:
                                if (currentFocus.getCol() > 0)
                                    dataGrid.tableIndex.get(currentFocus.getRow()).get(currentFocus.getCol() - 1).focus();
                                break;

                            case 63272:
                            case KeyboardListener.KEY_DELETE:
                                currentFocus.getWrappedWidget().setHTML("");
                                break;

                            case 32: // spacebar
                                currentFocus.edit();
                                return false;
                        }
                }
                return true;
            }
        });


    }

    public void setColumnHeader(int row, int column, String html) {
        cols = titleBar.ensureRowsAndCols(row + 1, column + 1);
        titleBar.getTableIndex().get(row).get(column).getWrappedWidget().setHTML(html);
    }

    public void setCell(int row, int column, String html) {
        cols = dataGrid.ensureRowsAndCols(row + 1, column + 1);
        dataGrid.getTableIndex().get(row).get(column).getWrappedWidget().setHTML(html);
    }

    public void setCols(int cols) {
        this.cols = cols;
    }

    public int getCols() {
        return cols;
    }

    private int checkWidth(int column) {
        if (columnWidths.size() - 1 < column) {
            for (int i = 0; i <= column; i++) {
                columnWidths.add(150);
            }
        }

        return columnWidths.get(column);
    }

    public void updateWidth(int column, int width) {
        HTMLTable.ColumnFormatter colFormatter = titleBar.getTable().getColumnFormatter();
        colFormatter.setWidth(column, width + "px");

        colFormatter = dataGrid.getTable().getColumnFormatter();
        colFormatter.setWidth(column, width + "px");

        checkWidth(column);
        columnWidths.set(column, width);
    }

    public WSCell getCell(int row, int col) {
        return dataGrid.getCell(row, col);
    }

    private void selectColumn(int col) {
        for (ArrayList<WSCell> row : titleBar.getTableIndex()) {
            row.get(col).addStyleDependentName("hcolselect");
        }

        for (ArrayList<WSCell> row : dataGrid.getTableIndex()) {
            row.get(col).addStyleDependentName("colselect");
        }
    }

    private void blurColumn(int col) {
        for (ArrayList<WSCell> row : titleBar.getTableIndex()) {
            row.get(col).removeStyleDependentName("hcolselect");
        }

        for (ArrayList<WSCell> row : dataGrid.getTableIndex()) {
            row.get(col).removeStyleDependentName("colselect");
        }
    }


    public class WSAbstractGrid extends Composite {
        private ScrollPanel scrollPanel;
        private FlexTable table;
        private ArrayList<ArrayList<WSCell>> tableIndex;

        private GridType type;

        public WSAbstractGrid() {
            this(false, GridType.EDITABLE_GRID);
        }

        public WSAbstractGrid(GridType type) {
            this(false, type);
        }

        public WSAbstractGrid(boolean scrollable, GridType type) {
            //   initWidget(table = new FlexTable());

            this.type = type;
            table = new FlexTable();
            table.setStylePrimaryName("WSGrid");
            table.insertRow(0);

            if (scrollable) {
                scrollPanel = new ScrollPanel(table);
                initWidget(scrollPanel);
            }
            else {
                initWidget(table);
            }


            tableIndex = new ArrayList<ArrayList<WSCell>>();
            tableIndex.add(new ArrayList<WSCell>());


        }

        public void addCell(int row, HTML w) {
            int currentColSize = table.getCellCount(row);

            table.addCell(row);

            table.setWidget(row, currentColSize, new WSCell(this, w, row, currentColSize));
        }

        public void addRow() {
            table.insertRow(table.getRowCount());
            for (int i = 0; i < cols; i++) {
                addCell(table.getRowCount() - 1, new HTML());
            }
        }

        public int ensureRowsAndCols(int rows, int cols) {
            if (colSizes.size() < cols) {
                for (int i = 0; i < cols; i++) {
                    colSizes.add(200);
                }
            }

            if (table.getRowCount() == 0) {
                addRow();
            }

            while (table.getRowCount() < rows) {
                addRow();
            }

            for (int r = 0; r < table.getRowCount(); r++) {
                if (table.getCellCount(r) < cols) {
                    int growthDelta = cols - table.getCellCount(r);

                    for (int c = 0; c < growthDelta; c++) {
                        table.getColumnFormatter().setWidth(c, colSizes.get(c) + "px");

                        addCell(r, new HTML());
                    }

                    assert table.getCellCount(r) == cols : "New size is wrong: " + table.getCellCount(r);
                }
            }

            return cols == 0 ? 1 : cols;
        }

        public FlexTable getTable() {
            return table;
        }

        public void setHeight(String height) {
            if (scrollPanel != null) scrollPanel.setHeight(height);
        }

        public void setWidth(String width) {
            if (scrollPanel != null) scrollPanel.setWidth(width);
        }

        public int getOffsetHeight() {
            if (scrollPanel != null) return scrollPanel.getOffsetHeight();
            else return table.getOffsetHeight();
        }

        public int getOffsetWidth() {
            if (scrollPanel != null) return scrollPanel.getOffsetWidth();
            else return table.getOffsetWidth();
        }

        @Override
        protected void onAttach() {
            super.onAttach();

        }

        public ArrayList<ArrayList<WSCell>> getTableIndex() {
            return tableIndex;
        }

        public WSCell getCell(int row, int col) {
            return tableIndex.get(row).get(col);
        }


    }

    public class WSCell extends Composite {
        private SimplePanel panel;
        private HTML wrappedWidget;
        private boolean edit;
        private TextBox textBox;

        private int row;
        private int col;

        private WSAbstractGrid grid;

        public WSCell(WSAbstractGrid grid, HTML widget, int row, int col) {
            this.grid = grid;
            panel = new SimplePanel();
            textBox = new TextBox();
            textBox.setStylePrimaryName("WSCell-editbox");

            textBox.addFocusListener(new FocusListener() {
                public void onFocus(Widget sender) {
                }

                public void onLostFocus(Widget sender) {
                    stopedit();
                }
            });

            if (grid.tableIndex.size() - 1 < row) {
                while (grid.tableIndex.size() - 1 < row) {
                    grid.tableIndex.add(new ArrayList<WSCell>());
                }
            }
            ArrayList<WSCell> cols = grid.tableIndex.get(row);

            if (cols.size() == 0 || cols.size() - 1 < col) {
                cols.add(this);
            }
            else {
                cols.set(col, this);
            }

            this.wrappedWidget = widget;
            panel.add(wrappedWidget);

            this.row = row;
            this.col = col;

            initWidget(panel);
            setWidth(colSizes.get(col) + "px");
            setStyleName("WSCell");

            sinkEvents(Event.MOUSEEVENTS | Event.FOCUSEVENTS | Event.ONCLICK | Event.ONDBLCLICK);
        }

        public void edit() {
            panel.remove(wrappedWidget);

            textBox.setWidth(getOffsetWidth() + "px");
            textBox.setText(wrappedWidget.getHTML());
            panel.add(textBox);

            edit = true;

            textBox.selectAll();
            textBox.setFocus(true);
        }

        public void stopedit() {
            if (edit) {
                wrappedWidget.setHTML(textBox.getText());
                panel.remove(textBox);
                panel.add(wrappedWidget);

                edit = false;
            }
        }

        public void blur() {
            stopedit();
            removeStyleDependentName("selected");

            if (currentFocusRowColSpan) {
                blurColumn(col);
                currentFocusRowColSpan = false;
            }
        }

        public void focus() {
            if (currentFocus != null && currentFocus != this) {
                currentFocus.blur();
            }
            currentFocus = this;

            addStyleDependentName("selected");
        }

        public int getRow() {
            return row;
        }

        public int getCol() {
            return col;
        }

        public HTML getWrappedWidget() {
            return wrappedWidget;
        }

        public Style getStyle() {
            return wrappedWidget.getElement().getStyle();
        }

        @Override
        public void onBrowserEvent(Event event) {
            int leftG = getAbsoluteLeft() + 10;
            int rightG = getAbsoluteLeft() + colSizes.get(col) - 10;

            switch (event.getTypeInt()) {
                case Event.ONMOUSEOVER:
                    if (!_resizing) addStyleDependentName("hover");
                    break;
                case Event.ONMOUSEOUT:
                    if (!_resizing) removeStyleDependentName("hover");
                    break;

                case Event.ONMOUSEMOVE:

                    if (_resizing) {
                        //        DOM.setStyleAttribute(resizeLine.getElement(), "left", event.getClientX() + 1 + "px");
                    }
                    else {
                        if (event.getClientX() < leftG) {
                            addStyleDependentName("resize-left");
                            _resizeArmed = true;
                        }
                        else if (event.getClientX() > rightG) {
                            addStyleDependentName("resize-right");
                            _resizeArmed = true;
                        }
                        else {
                            removeStyleDependentName("resize-left");
                            removeStyleDependentName("resize-right");
                            _resizeArmed = false;
                        }
                    }
                    break;

                case Event.ONMOUSEDOWN:
                    if (_resizeArmed) {
                        if (!_resizing) {
                            _resizing = true;
                            disableTextSelection(RootPanel.getBodyElement(), true);

                            focus();
                            resizeLine.show();
                            resizeLine.setPopupPosition(event.getClientX() + 1, 0);
                            _rsize = event.getClientX();
                        }

                    }

                    break;

                case Event.ONFOCUS:
                    break;

                case Event.ONCLICK:
                    break;

                case Event.ONDBLCLICK:
                    switch (grid.type) {
                        case EDITABLE_GRID:
                            edit();
                            break;
                        case TITLEBAR:
                            // do sorting here
                            break;
                    }
                    break;

                case Event.ONMOUSEUP:
                    switch (grid.type) {
                        case EDITABLE_GRID:
                            if (!_resizing) focus();
                            break;
                        case TITLEBAR:
                            if (!_resizing) {
                                focus();
                                currentFocusRowColSpan = true;
                                selectColumn(col);
                            }
                            break;
                    }

                    break;
            }
        }
    }

    @Override
    public void onBrowserEvent(Event event) {
        switch (event.getTypeInt()) {
            case Event.ONMOUSEMOVE: {
                DOM.setStyleAttribute(resizeLine.getElement(), "left", event.getClientX() + "px");
                break;
            }
            case Event.ONMOUSEUP: {
                if (_resizing) {
                    cancelMove();

                    int width = colSizes.get(currentFocus.col);

                    _rsize -= event.getClientX();
                    width -= _rsize;

                    colSizes.set(currentFocus.col, width);

                    titleBar.getTable().getColumnFormatter().setWidth(currentFocus.col, width + "px");
                    dataGrid.getTable().getColumnFormatter().setWidth(currentFocus.col, width + "px");

                    titleBar.tableIndex.get(0).get(currentFocus.col).setWidth(width + "px");

                    for (int cX = 0; cX < dataGrid.tableIndex.size(); cX++) {
                        dataGrid.tableIndex.get(cX).get(currentFocus.col).setWidth(width + "px");
                    }

                    disableTextSelection(RootPanel.getBodyElement(), false);
                }
            }
        }
    }

    private void cancelMove() {
        resizeLine.hide();
        _resizing = false;
        _resizeArmed = false;
    }

    public void setHeight(String height) {
        panel.setHeight(height);
    }

    public void setWidth(String width) {
        panel.setWidth(width);
    }

    @Override
    protected void onAttach() {
        super.onAttach();
        panel.setCellHeight(titleBar, titleBar.getOffsetHeight() + "px");
        dataGrid.setHeight("100%");
    }

    public static void disableTextSelection(Element elem, boolean
            disable) {
        setStyleName(elem, "my-no-selection", disable);
        disableTextSelectInternal(elem, disable);
    }

    private native static void disableTextSelectInternal(Element e, boolean disable)/*-{
      if (disable) {
        e.ondrag = function () { return false; };
        e.onselectstart = function () { return false; };
      } else {
        e.ondrag = null;
        e.onselectstart = null;
      }
    }-*/;

    private static final int EDITABLE = 1;
    private static final int TITLEGRID = 1 << 1;

    public enum GridType {
        EDITABLE_GRID(EDITABLE),
        TITLEBAR(TITLEGRID);

        private int options;

        GridType(int options) {
            this.options = options;
        }

        public boolean isEditable() {
            return (EDITABLE & options) != 0;
        }

        public boolean isTitleGrid() {
            return (TITLEGRID & options) != 0;
        }
    }
}