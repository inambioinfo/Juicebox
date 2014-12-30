/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2011-2014 Broad Institute, Aiden Lab
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */


package juicebox;

import juicebox.data.*;
import juicebox.track.*;
import juicebox.windowui.HiCZoom;
import juicebox.windowui.MatrixType;
import juicebox.windowui.NormalizationType;
import org.apache.log4j.Logger;
import org.broad.igv.Globals;
import org.broad.igv.feature.Chromosome;
import org.broad.igv.ui.color.ColorUtilities;
import org.broad.igv.util.ParsingUtils;
import org.broad.igv.util.ResourceLocator;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;
import java.util.List;

/**
 * This is the "model" class for the HiC viewer.
 *
 * @author Jim Robinson
 * @since 4/8/12
 */
public class HiC {

    private static final Logger log = Logger.getLogger(HiC.class);
    private final MainWindow mainWindow;
    private final Map<String, Feature2DList> loopLists;
    private final HiCTrackManager trackManager;
    private double scaleFactor;
    private String xPosition;
    private String yPosition;
    private MatrixType displayOption;
    private NormalizationType normalizationType;
    private java.util.List<Chromosome> chromosomes;
    private Dataset dataset;
    private Dataset controlDataset;
    private HiCZoom zoom;
    private Context xContext;
    private Context yContext;
    private boolean showLoops;

    private EigenvectorTrack eigenvectorTrack;
    private ResourceTree resourceTree;
    private LoadEncodeAction encodeAction;
    private Point cursorPoint;
    private Point selectedBin;
    private boolean linkedMode;
    private boolean m_zoomChanged;
    private boolean m_displayOptionChanged;
    private boolean m_normalizationTypeChanged;
    private HashMap<String, Integer> binSizeDictionary = new HashMap<String, Integer>();

    public HiC(MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        this.trackManager = new HiCTrackManager(mainWindow, this);
        this.loopLists = new HashMap<String, Feature2DList>();
        this.m_zoomChanged = false;
        this.m_displayOptionChanged = false;
        this.m_normalizationTypeChanged = false;
        initBinSizeDictionary();
    }

    public static boolean isPrivateHic(String string) {
        return string.contains("igvdata/hic/files");
    }

    public void reset() {
        dataset = null;
        xContext = null;
        yContext = null;
        eigenvectorTrack = null;
        resourceTree = null;
        encodeAction = null;
        trackManager.clearTracks();
        loopLists.clear();
        showLoops = true;
    }

    public double getScaleFactor() {
        return scaleFactor;
    }

    public void setScaleFactor(double scaleFactor) {
        this.scaleFactor = Math.min(50, scaleFactor);
    }

    public void setControlDataset(Dataset controlDataset) {
        this.controlDataset = controlDataset;
    }

    public void loadEigenvectorTrack() {
        if (eigenvectorTrack == null) {
            eigenvectorTrack = new EigenvectorTrack("Eigenvector", "Eigenvector", this);
        }
        trackManager.add(eigenvectorTrack);
    }


    public ResourceTree getResourceTree() {
        return resourceTree;
    }

    public void setResourceTree(ResourceTree rTree) {
        resourceTree = rTree;
    }

    public LoadEncodeAction getEncodeAction() {
        return encodeAction;
    }

    public void setEncodeAction(LoadEncodeAction eAction) {
        encodeAction = eAction;
    }

    public List<Feature2D> getVisibleLoopList(int chrIdx1, int chrIdx2) {
        if (!showLoops) return null;
        List<Feature2D> visibleLoopList = new ArrayList<Feature2D>();
        for (Feature2DList list : loopLists.values()) {
            if (list.isVisible()) {
                List<Feature2D> currList = list.get(chrIdx1, chrIdx2);
                if (currList != null) {
                    for (Feature2D feature2D : currList) {
                        visibleLoopList.add(feature2D);
                    }
                }
            }
        }
        return visibleLoopList;
    }

    public void setShowLoops(boolean showLoops1) {
        showLoops = showLoops1;

    }

    public void setLoopsInvisible(String path) {
        loopLists.get(path).setVisible(false);
    }

    public boolean isLinkedMode() {
        return linkedMode;
    }

    public void setLinkedMode(boolean linkedMode) {
        this.linkedMode = linkedMode;
    }

    public java.util.List<HiCTrack> getLoadedTracks() {
        return trackManager == null ? new ArrayList<HiCTrack>() : trackManager.getLoadedTracks();
    }

    public void loadHostedTracks(List<ResourceLocator> locators) {
        trackManager.safeTrackLoad(locators);
    }

    public void loadTrack(String path) {
        trackManager.loadTrack(path);
    }

    public void loadCoverageTrack(NormalizationType no) {
        trackManager.loadCoverageTrack(no);
    }

    public void removeTrack(HiCTrack track) {
        if (resourceTree != null) resourceTree.remove(track.getLocator());
        if (encodeAction != null) encodeAction.remove(track.getLocator());
        trackManager.removeTrack(track);
    }

    public void removeTrack(ResourceLocator locator) {
        if (resourceTree != null) resourceTree.remove(locator);
        if (encodeAction != null) encodeAction.remove(locator);
        trackManager.removeTrack(locator);
    }

    public Dataset getDataset() {
        return dataset;
    }

    public void setDataset(Dataset dataset) {
        this.dataset = dataset;
    }

    public void setSelectedChromosomes(Chromosome chrX, Chromosome chrY) {
        this.xContext = new Context(chrX);
        this.yContext = new Context(chrY);

        if (eigenvectorTrack != null) {
            eigenvectorTrack.forceRefresh();
        }

    }

    public HiCZoom getZoom() {
        return zoom;
    }

    public MatrixZoomData getZd() {
        Matrix matrix = getMatrix();
        if (matrix == null || zoom == null) {
            return null;
        } else {
            return matrix.getZoomData(zoom);
        }
    }

    public MatrixZoomData getControlZd() {
        Matrix matrix = getControlMatrix();
        if (matrix == null || zoom == null) {
            return null;
        } else {
            return matrix.getZoomData(zoom);
        }
    }

    public Matrix getControlMatrix() {
        if (controlDataset == null || xContext == null || zoom == null) return null;

        return controlDataset.getMatrix(xContext.getChromosome(), yContext.getChromosome());
    }

    public Context getXContext() {
        return xContext;
    }

    public Context getYContext() {
        return yContext;
    }

    public void resetContexts() {
        this.xContext = null;
        this.yContext = null;
    }

    public Point getCursorPoint() {
        return cursorPoint;
    }

    public void setCursorPoint(Point point) {
        this.cursorPoint = point;

    }

    public String getXPosition() {
        return xPosition;
    }

    public void setXPosition(String txt) {
        this.xPosition = txt;
    }

    public String getYPosition() {
        return yPosition;
    }

    public void setYPosition(String txt) {
        this.yPosition = txt;
    }

    public Matrix getMatrix() {
        return dataset == null || xContext == null ? null : getDataset().getMatrix(xContext.getChromosome(), yContext.getChromosome());

    }

    public void setSelectedBin(Point point) {
        if (point.equals(this.selectedBin)) {
            this.selectedBin = null;
        } else {
            this.selectedBin = point;
        }
    }

    public MatrixType getDisplayOption() {
        return displayOption;
    }

    public void setDisplayOption(MatrixType newDisplay) {
        if (this.displayOption != newDisplay) {
            this.displayOption = newDisplay;
            setDisplayOptionChanged();
        }
    }

    public boolean isControlLoaded() {
        return controlDataset != null;
    }

    public boolean isWholeGenome() {
        return xContext != null && xContext.getChromosome().getName().equals("All");
    }

    public java.util.List<Chromosome> getChromosomes() {
        return chromosomes;
    }

    public void setChromosomes(List<Chromosome> chromosomes) {
        this.chromosomes = chromosomes;
    }

    /**
     * Change zoom level and recenter.  Triggered by the resolutionSlider, or by a double-click in the
     * heatmap panel.
     */
    public boolean setZoom(HiCZoom newZoom, final double centerGenomeX, final double centerGenomeY) {

        if (dataset == null) return false;

        final Chromosome chrX = xContext.getChromosome();
        final Chromosome chrY = yContext.getChromosome();

        // Verify that all datasets have this zoom level

        Matrix matrix = dataset.getMatrix(chrX, chrY);
        if (matrix == null) return false;

        MatrixZoomData newZD;
        if (chrX.getName().equals("All")) {
            newZD = matrix.getFirstZoomData(Unit.BP);
        } else {
            newZD = matrix.getZoomData(newZoom);
        }
        if (newZD == null) {
            JOptionPane.showMessageDialog(mainWindow, "Sorry, this zoom is not available", "Zoom unavailable", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        // Assumption is all datasets share the same grid axis
        HiCGridAxis xGridAxis = newZD.getXGridAxis();
        HiCGridAxis yGridAxis = newZD.getYGridAxis();

        zoom = newZoom;

        xContext.setZoom(zoom);
        yContext.setZoom(zoom);

        int xBinCount = xGridAxis.getBinCount();
        int yBinCount = yGridAxis.getBinCount();
        int maxBinCount = Math.max(xBinCount, yBinCount);

        double scalefactor = Math.max(1.0, (double) mainWindow.getHeatmapPanel().getMinimumDimension() / maxBinCount);

        setScaleFactor(scalefactor);

        //Point binPosition = zd.getBinPosition(genomePositionX, genomePositionY);
        int binX = xGridAxis.getBinNumberForGenomicPosition((int) centerGenomeX);
        int binY = yGridAxis.getBinNumberForGenomicPosition((int) centerGenomeY);

        center(binX, binY);

        //Notify Heatmap panel render that the zoom has been changed. In that case,
        //Render should update zoom slider (only once) with previous map range values
        setZoomChanged();

        if (linkedMode) {
            broadcastState();
        }

        return true;
    }

    private void setZoomChanged() {
        m_zoomChanged = true;
    }

    //Check zoom change value and reset.
    public synchronized boolean testZoomChanged() {
        if (m_zoomChanged) {
            m_zoomChanged = false;
            return true;
        }
        return false;
    }

    // Called from alt-drag
    public void zoomTo(final int xBP0, final int yBP0, double targetBinSize) {


        if (dataset == null) return;  // No data in view


        final Chromosome chr1 = xContext.getChromosome();
        final Chromosome chr2 = yContext.getChromosome();
        final Matrix matrix = dataset.getMatrix(chr1, chr2);


        HiC.Unit unit = zoom.getUnit();

        // Find the new resolution,
        HiCZoom newZoom = zoom;
        if (!mainWindow.isResolutionLocked()) {
            List<HiCZoom> zoomList = unit == HiC.Unit.BP ? dataset.getBpZooms() : dataset.getFragZooms();
            zoomList.get(zoomList.size() - 1);   // Highest zoom level by default
            for (int i = zoomList.size() - 1; i >= 0; i--) {
                if (zoomList.get(i).getBinSize() > targetBinSize) {
                    newZoom = zoomList.get(i);
                    break;
                }
            }
        }

        final MatrixZoomData newZD = matrix.getZoomData(newZoom);

        int binX0 = newZD.getXGridAxis().getBinNumberForGenomicPosition(xBP0);
        int binY0 = newZD.getYGridAxis().getBinNumberForGenomicPosition(yBP0);

        final double scaleFactor = newZD.getBinSize() / targetBinSize;

        zoom = newZD.getZoom();


        mainWindow.updateZoom(zoom);

        setScaleFactor(scaleFactor);

        xContext.setBinOrigin(binX0);
        yContext.setBinOrigin(binY0);


        if (linkedMode) {
            broadcastState();
        }

        try {
            mainWindow.refresh();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public void centerFragment(int fragmentX, int fragmentY) {
        if (zoom != null) {

            MatrixZoomData zd = getMatrix().getZoomData(zoom);
            HiCGridAxis xAxis = zd.getXGridAxis();
            HiCGridAxis yAxis = zd.getYGridAxis();
            int binX;
            int binY;
            try {
                binX = xAxis.getBinNumberForFragment(fragmentX);
                //noinspection SuspiciousNameCombination
                binY = yAxis.getBinNumberForFragment(fragmentY);
                center(binX, binY);
            } catch (RuntimeException error) {
                JOptionPane.showMessageDialog(mainWindow, error.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }

        }
    }

    public void centerBP(int bpX, int bpY) {
        if (zoom != null) {
            MatrixZoomData zd = getMatrix().getZoomData(zoom);
            HiCGridAxis xAxis = zd.getXGridAxis();
            HiCGridAxis yAxis = zd.getYGridAxis();

            int binX = xAxis.getBinNumberForGenomicPosition(bpX);
            int binY = yAxis.getBinNumberForGenomicPosition(bpY);
            center(binX, binY);

        }
    }

    /**
     * Center the bins in view at the current resolution.
     *
     * @param binX center X
     * @param binY center Y
     */
    public void center(double binX, double binY) {

        double w = mainWindow.getHeatmapPanel().getWidth() / getScaleFactor();  // view width in bins
        int newOriginX = (int) (binX - w / 2);

        double h = mainWindow.getHeatmapPanel().getHeight() / getScaleFactor();  // view hieght in bins
        int newOriginY = (int) (binY - h / 2);
        moveTo(newOriginX, newOriginY);
    }

    /**
     * Move by the specified delta (in bins)
     *
     * @param dxBins -- delta x in bins
     * @param dyBins -- delta y in bins
     */
    public void moveBy(double dxBins, double dyBins) {
        final double newX = xContext.getBinOrigin() + dxBins;
        final double newY = yContext.getBinOrigin() + dyBins;
        moveTo(newX, newY);
    }

    /**
     * Move to the specified origin (in bins)
     *
     * @param newBinX new location X
     * @param newBinY new location Y
     */
    private void moveTo(double newBinX, double newBinY) {

        MatrixZoomData zd = getZd();

        final double wBins = (mainWindow.getHeatmapPanel().getWidth() / getScaleFactor());
        double maxX = zd.getXGridAxis().getBinCount() - wBins;

        final double hBins = (mainWindow.getHeatmapPanel().getHeight() / getScaleFactor());
        double maxY = zd.getYGridAxis().getBinCount() - hBins;

        double x = Math.max(0, Math.min(maxX, newBinX));
        double y = Math.max(0, Math.min(maxY, newBinY));

        xContext.setBinOrigin(x);
        yContext.setBinOrigin(y);

//        String locus1 = "chr" + (xContext.getChromosome().getName()) + ":" + x + "-" + (int) (x + bpWidthX);
//        String locus2 = "chr" + (yContext.getChromosome().getName()) + ":" + x + "-" + (int) (y + bpWidthY);
//        IGVUtils.sendToIGV(locus1, locus2);

        mainWindow.repaint();

        if (linkedMode) {
            broadcastState();
        }
    }

    private void setDisplayOptionChanged() {
        m_displayOptionChanged = true;
    }

    //Check zoom change value and reset.
    public synchronized boolean testDisplayOptionChanged() {
        if (m_displayOptionChanged) {
            m_displayOptionChanged = false;
            return true;
        }
        return false;
    }

    private void setNormalizationTypeChanged() {
        m_normalizationTypeChanged = true;
    }

    //Check zoom change value and reset.
    public synchronized boolean testNormalizationTypeChanged() {
        if (m_normalizationTypeChanged) {
            m_normalizationTypeChanged = false;
            return true;
        }
        return false;
    }

    public NormalizationType getNormalizationType() {
        return normalizationType;
    }

    public void setNormalizationType(NormalizationType option) {
        if (this.normalizationType != option) {
            this.normalizationType = option;
            setNormalizationTypeChanged();
        }
    }

    public double[] getEigenvector(final int chrIdx, final int n) {

        if (dataset == null) return null;

        Chromosome chr = chromosomes.get(chrIdx);
        return dataset.getEigenvector(chr, zoom, n, normalizationType);

    }

    public ExpectedValueFunction getExpectedValues() {
        if (dataset == null) return null;
        return dataset.getExpectedValues(zoom, normalizationType);
    }

    public NormalizationVector getNormalizationVector(int chrIdx) {
        if (dataset == null) return null;
        return dataset.getNormalizationVector(chrIdx, zoom, normalizationType);
    }

    // Note - this is an inefficient method, used to support tooltip text only.
    public float getNormalizedObservedValue(int binX, int binY) {

        return getZd().getObservedValue(binX, binY, normalizationType);

    }

    public void loadLoopList(String path) throws IOException {

        if (loopLists.get(path) != null) {
            loopLists.get(path).setVisible(true);
            return;
        }

        int attCol = 7;
        BufferedReader br = null;

        Feature2DList newList = new Feature2DList();



        try {
            br = ParsingUtils.openBufferedReader(path);
            String nextLine;

            // header
            nextLine = br.readLine();
            String[] headers = Globals.tabPattern.split(nextLine);

            int errorCount = 0;
            int lineNum = 1;
            while ((nextLine = br.readLine()) != null) {
                lineNum++;
                String[] tokens = Globals.tabPattern.split(nextLine);
                if (tokens.length > headers.length) {
                    throw new IOException("Improperly formatted file");
                }
                if (tokens.length < 6) {
                    continue;
                }

                String chr1Name, chr2Name;
                int start1, end1, start2, end2;
                try {
                    chr1Name = tokens[0];
                    start1 = Integer.parseInt(tokens[1]);
                    end1 = Integer.parseInt(tokens[2]);

                    chr2Name = tokens[3];
                    start2 = Integer.parseInt(tokens[4]);
                    end2 = Integer.parseInt(tokens[5]);
                } catch (Exception e) {
                    throw new IOException("Line " + lineNum + " improperly formatted in <br>" +
                            path + "<br>Line format should start with:  CHR1  X1  X2  CHR2  Y1  Y2");
                }


                Color c = tokens.length > 6 ? ColorUtilities.stringToColor(tokens[6].trim()) : Color.black;

                Map<String, String> attrs = new LinkedHashMap<String, String>();
                for (int i = attCol; i < tokens.length; i++) {

                    attrs.put(headers[i], tokens[i]);
                }

                Chromosome chr1 = this.getChromosomeNamed(chr1Name);
                Chromosome chr2 = this.getChromosomeNamed(chr2Name);
                if (chr1 == null || chr2 == null) {
                    if (errorCount < 100) {
                        log.debug("Skipping line: " + nextLine);
                    } else if (errorCount == 100) {
                        log.debug("Maximum error count exceeded.  Further errors will not be logged");
                    }

                    errorCount++;
                    continue;
                }

                int featureNameSepindex = path.lastIndexOf("_");
                String featureName = path.substring(featureNameSepindex + 1);

                if (featureName.equals("blocks.txt")){
                    featureName = "Contact domain";
                }
                else if (featureName.equals("peaks.txt")){
                    featureName = "Peak";
                }
                else
                {
                    featureName = "Feature";
                }
                // Convention is chr1 is lowest "index". Swap if necessary
                Feature2D feature = chr1.getIndex() <= chr2.getIndex() ?
                        new Feature2D(featureName, chr1Name, start1, end1, chr2Name, start2, end2, c, attrs) :
                        new Feature2D(featureName, chr2Name, start2, end2, chr1Name, start1, end1, c, attrs);

                newList.add(chr1.getIndex(), chr2.getIndex(), feature);

            }
            loopLists.put(path, newList);
        } finally {
            if (br != null) br.close();
        }

    }

    private Chromosome getChromosomeNamed(String token) {
        for (Chromosome chr : chromosomes) {
            if (token.toLowerCase().equals(chr.getName().toLowerCase()) || String.valueOf("chr").concat(token.toLowerCase()).equals(chr.getName().toLowerCase()) || token.toLowerCase().equals(String.valueOf("chr").concat(chr.getName().toLowerCase())))
                return chr;
        }
        return null;
    }

    /**
     * Change zoom level and recenter.  Triggered by the resolutionSlider, or by a double-click in the
     * heatmap panel.
     */
    public void setState(String chrXName, String chrYName, String unitName, int binSize, double xOrigin, double yOrigin, double scalefactor) {

        if (!chrXName.equals(xContext.getChromosome().getName()) || !chrYName.equals(yContext.getChromosome().getName())) {

            Chromosome chrX = getChromosomeNamed(chrXName);
            Chromosome chrY = getChromosomeNamed(chrYName);

            if (chrX == null || chrY == null) {
                //Chromosomes do not appear to exist in current map.
                log.info("Chromosome(s) not found.");
                log.info("Most probably origin is a different species saved location or sync/link between two different species maps.");
                return;
            }


            this.xContext = new Context(chrX);
            this.yContext = new Context(chrY);
            mainWindow.setSelectedChromosomesNoRefresh(chrX, chrY);
            if (eigenvectorTrack != null) {
                eigenvectorTrack.forceRefresh();
            }
        }

        HiCZoom newZoom = new HiCZoom(Unit.valueOf(unitName), binSize);
        if (!newZoom.equals(zoom) || (xContext.getZoom() == null) || (yContext.getZoom() == null)) {
            zoom = newZoom;
            xContext.setZoom(zoom);
            yContext.setZoom(zoom);
            mainWindow.updateZoom(newZoom);
        }

        //setScaleFactor(scalefactor);
        setScaleFactor(scalefactor);
        xContext.setBinOrigin(xOrigin);
        yContext.setBinOrigin(yOrigin);

        try {
            mainWindow.refresh();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void broadcastState() {
        String xChr = xContext.getChromosome().getName();
        String yChr = yContext.getChromosome().getName();

        if (!(xChr.toLowerCase().contains("chr"))) {
            xChr = "chr" + xChr;
        }
        if (!(yChr.toLowerCase().contains("chr"))) {
            yChr = "chr" + yChr;
        }

        String command = "setstate " +
                xChr + " " +
                yChr + " " +
                zoom.getUnit().toString() + " " +
                zoom.getBinSize() + " " +
                xContext.getBinOrigin() + " " +
                yContext.getBinOrigin() + " " +
                getScaleFactor();

        CommandBroadcaster.broadcast(command);
    }

    public String saveState() {
        String xChr = xContext.getChromosome().getName();
        String yChr = yContext.getChromosome().getName();

        if (!(xChr.toLowerCase().contains("chr"))) {
            xChr = "chr" + xChr;
        }
        if (!(yChr.toLowerCase().contains("chr"))) {
            yChr = "chr" + yChr;
        }

        String command = "setstate " +
                xChr + " " +
                yChr + " " +
                zoom.getUnit().toString() + " " +
                zoom.getBinSize() + " " +
                xContext.getBinOrigin() + " " +
                yContext.getBinOrigin() + " " +
                getScaleFactor();

        return command;
        // CommandBroadcaster.broadcast(command);
    }

    public String getDefaultLocationDescription() {

        String xChr = xContext.getChromosome().getName();
        String yChr = yContext.getChromosome().getName();

        if (!(xChr.toLowerCase().contains("chr"))) {
            xChr = "chr" + xChr;
        }
        if (!(yChr.toLowerCase().contains("chr"))) {
            yChr = "chr" + yChr;
        }

        String command = xChr + "@" +
                (long) (xContext.getBinOrigin() * zoom.getBinSize()) + "_" +
                yChr + "@" +
                (long) (yContext.getBinOrigin() * zoom.getBinSize());

        return command;
        // CommandBroadcaster.broadcast(command);
    }

    public void restoreState(String cmd) {
        CommandExecutor cmdExe = new CommandExecutor(this);
        cmdExe.execute(cmd);
        if (linkedMode) {
            broadcastState();
        }
    }

    public enum Unit {BP, FRAG}

    public java.lang.Integer validteBinSize(String key)
    {
        if (binSizeDictionary.containsKey(key))
        {
            return Integer.valueOf(String.valueOf(binSizeDictionary.get(key)));
        }
        else
        {
            return null;
        }
    }

    private void initBinSizeDictionary(){
        //BP Bin size:
        binSizeDictionary.put("2.5M",2500000);
        binSizeDictionary.put("1M",1000000);
        binSizeDictionary.put("500K",500000);
        binSizeDictionary.put("250K",250000);
        binSizeDictionary.put("100K",100000);
        binSizeDictionary.put("50K",50000);
        binSizeDictionary.put("25K",25000);
        binSizeDictionary.put("10K",10000);
        binSizeDictionary.put("5K",5000);
        binSizeDictionary.put("1K",1000);
        binSizeDictionary.put("2.5m",2500000);
        binSizeDictionary.put("1m",1000000);
        binSizeDictionary.put("500k",500000);
        binSizeDictionary.put("250k",250000);
        binSizeDictionary.put("100k",100000);
        binSizeDictionary.put("50k",50000);
        binSizeDictionary.put("25k",25000);
        binSizeDictionary.put("10k",10000);
        binSizeDictionary.put("5k",5000);
        binSizeDictionary.put("1k",1000);
        binSizeDictionary.put("2500000",2500000);
        binSizeDictionary.put("1000000",1000000);
        binSizeDictionary.put("500000",500000);
        binSizeDictionary.put("250000",250000);
        binSizeDictionary.put("100000",100000);
        binSizeDictionary.put("50000",50000);
        binSizeDictionary.put("25000",25000);
        binSizeDictionary.put("10000",10000);
        binSizeDictionary.put("5000",5000);
        binSizeDictionary.put("1000",1000);

        //FRAG Bin size:
        binSizeDictionary.put("500f",500);
        binSizeDictionary.put("200f",200);
        binSizeDictionary.put("100f",100);
        binSizeDictionary.put("50f",50);
        binSizeDictionary.put("20f",20);
        binSizeDictionary.put("5f",5);
        binSizeDictionary.put("2f",2);
        binSizeDictionary.put("1f",1);
    }
}

