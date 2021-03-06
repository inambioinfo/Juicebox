/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2011-2020 Broad Institute, Aiden Lab, Rice University, Baylor College of Medicine
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

package juicebox.tools.utils.norm;

import juicebox.HiC;
import juicebox.HiCGlobals;
import juicebox.data.*;
import juicebox.data.basics.Chromosome;
import juicebox.data.basics.ListOfDoubleArrays;
import juicebox.data.basics.ListOfFloatArrays;
import juicebox.tools.utils.original.ExpectedValueCalculation;
import juicebox.windowui.HiCZoom;
import juicebox.windowui.NormalizationHandler;
import juicebox.windowui.NormalizationType;
import org.broad.igv.tdf.BufferedByteWriter;
import org.broad.igv.util.Pair;

import java.io.IOException;
import java.util.*;

public class GenomeWideNormalizationVectorUpdater extends NormVectorUpdater {
    public static void addGWNorm(String path, int genomeWideResolution) throws IOException {
        DatasetReaderV2 reader = new DatasetReaderV2(path);
        Dataset ds = reader.read();
        HiCGlobals.verifySupportedHiCFileVersion(reader.getVersion());

        List<HiCZoom> resolutions = new ArrayList<>();
        resolutions.addAll(ds.getBpZooms());
        resolutions.addAll(ds.getFragZooms());

        List<BufferedByteWriter> normVectorBuffers = new ArrayList<>();
        List<NormalizationVectorIndexEntry> normVectorIndex = new ArrayList<>();
        Map<String, ExpectedValueFunction> expectedValueFunctionMap = ds.getExpectedValueFunctionMap();

        for (Iterator<Map.Entry<String, ExpectedValueFunction>> it = expectedValueFunctionMap.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String, ExpectedValueFunction> entry = it.next();
            if (entry.getKey().contains("NONE")) {
                it.remove();
            }
        }

        // Loop through resolutions
        for (HiCZoom zoom : resolutions) {

            // compute genome-wide normalization
            // TODO make this dependent on memory, do as much as possible
            if (genomeWideResolution >= 10000 && zoom.getUnit() == HiC.Unit.BP && zoom.getBinSize() >= genomeWideResolution) {
                for (NormalizationType normType : NormalizationHandler.getAllGWNormTypes(false)) {

                    Pair<Map<Chromosome, NormalizationVector>, ExpectedValueCalculation> wgVectors = getWGVectors(ds, zoom, normType);

                    if (wgVectors != null) {
                        Map<Chromosome, NormalizationVector> nvMap = wgVectors.getFirst();
                        for (Chromosome chromosome : nvMap.keySet()) {
                            updateNormVectorIndexWithVector(normVectorIndex, normVectorBuffers, nvMap.get(chromosome).getData().convertToFloats(), chromosome.getIndex(), normType, zoom);
                        }
                        ExpectedValueCalculation calculation = wgVectors.getSecond();
                        String key = ExpectedValueFunctionImpl.getKey(zoom, normType);
                        expectedValueFunctionMap.put(key, calculation.getExpectedValueFunction());
                    }
                }
            }
            System.out.println();
            System.out.print("Calculating norms for zoom " + zoom);

            /*
            // Integer is either limit on genome wide resolution or limit on what fragment resolution to calculate
            if (genomeWideResolution == 0 && zoom.getUnit() == HiC.Unit.FRAG) continue;
            if (genomeWideResolution < 10000 && zoom.getUnit() == HiC.Unit.FRAG && zoom.getBinSize() <= genomeWideResolution) continue;
            */

            // Loop through chromosomes
            for (Chromosome chr : ds.getChromosomeHandler().getChromosomeArrayWithoutAllByAll()) {
                Matrix matrix = ds.getMatrix(chr, chr);
                if (matrix == null) continue;

                for (NormalizationType normType : NormalizationHandler.getAllNormTypes()) {
                    NormalizationVector vector = ds.getNormalizationVector(chr.getIndex(), zoom, normType);
                    if (vector != null) {
                        updateNormVectorIndexWithVector(normVectorIndex, normVectorBuffers, vector.getData().convertToFloats(), chr.getIndex(), normType, zoom);
                    }
                }
            }
        }

        int version = reader.getVersion();
        long filePosition = reader.getNormFilePosition();
        reader.close();
        System.out.println();
        NormalizationVectorUpdater.update(path, version, filePosition, expectedValueFunctionMap, normVectorIndex,
                normVectorBuffers);
        System.out.println("Finished normalization");
    }


    public static void updateHicFileForGWfromPreAddNormOnly(Dataset ds, HiCZoom zoom, List<NormalizationType> normalizationsToBuild,
                                                            Map<NormalizationType, Integer> resolutionsToBuildTo, List<NormalizationVectorIndexEntry> normVectorIndices,
                                                            List<BufferedByteWriter> normVectorBuffers, List<ExpectedValueCalculation> expectedValueCalculations) throws IOException {
        for (NormalizationType normType : normalizationsToBuild) {
            if (NormalizationHandler.isGenomeWideNorm(normType)) {
                if (zoom.getBinSize() >= resolutionsToBuildTo.get(normType)) {

                    long currentTime = System.currentTimeMillis();
                    Pair<Map<Chromosome, NormalizationVector>, ExpectedValueCalculation> wgVectors = getWGVectors(ds, zoom, normType);
                    if (HiCGlobals.printVerboseComments) {
                        System.out.println("\n" + normType.getLabel() + " normalization genome wide at " + zoom + " took " + (System.currentTimeMillis() - currentTime) + " milliseconds");
                    }

                    if (wgVectors != null) {
                        Map<Chromosome, NormalizationVector> nvMap = wgVectors.getFirst();
                        for (Chromosome chromosome : nvMap.keySet()) {
                            updateNormVectorIndexWithVector(normVectorIndices, normVectorBuffers, nvMap.get(chromosome).getData().convertToFloats(), chromosome.getIndex(), normType, zoom);
                        }

                        expectedValueCalculations.add(wgVectors.getSecond());
                    }
                }
            }
        }
    }

    /**
     * Compute the whole-genome normalization and expected value vectors and return as a pair (normalization vector first)
     */

    private static Pair<Map<Chromosome, NormalizationVector>, ExpectedValueCalculation> getWGVectors(Dataset dataset,
                                                                                                     HiCZoom zoom,
                                                                                                     NormalizationType norm) {
        boolean includeIntraData = NormalizationHandler.isGenomeWideNormIntra(norm); // default INTER type
        final ChromosomeHandler chromosomeHandler = dataset.getChromosomeHandler();
        final int resolution = zoom.getBinSize();
        final List<List<ContactRecord>> recordArrayList = createWholeGenomeRecords(dataset, chromosomeHandler, zoom, includeIntraData);

        int totalSize = 0;
        for (Chromosome c1 : chromosomeHandler.getChromosomeArrayWithoutAllByAll()) {
            totalSize += c1.getLength() / resolution + 1;
        }
    
        NormalizationCalculations calculations = new NormalizationCalculations(recordArrayList, totalSize);
        ListOfFloatArrays vector = calculations.getNorm(norm);

        if (vector == null) {
            return null;
        }

        ExpectedValueCalculation expectedValueCalculation = new ExpectedValueCalculation(chromosomeHandler, resolution, null, norm);
        int addY = 0;
        // Loop through chromosomes
        for (Chromosome chr : chromosomeHandler.getChromosomeArrayWithoutAllByAll()) {
            final int chrIdx = chr.getIndex();

            MatrixZoomData zd = HiCFileTools.getMatrixZoomData(dataset, chr, chr, zoom);
            if (zd == null) continue;

            for (List<ContactRecord> crList : zd.getContactRecordList()) {
                for (ContactRecord cr : crList) {
                    int x = cr.getBinX();
                    int y = cr.getBinY();
                    final float vx = vector.get(x + addY);
                    final float vy = vector.get(y + addY);
                    if (isValidNormValue(vx) && isValidNormValue(vy)) {
                        double value = cr.getCounts() / (vx * vy);
                        expectedValueCalculation.addDistance(chrIdx, x, y, value);
                    }
                }
            }

            addY += chr.getLength() / resolution + 1;
        }

        // Split normalization vector by chromosome
        Map<Chromosome, NormalizationVector> normVectorMap = new LinkedHashMap<>();
        long location1 = 0;
        for (Chromosome c1 : chromosomeHandler.getChromosomeArrayWithoutAllByAll()) {
            long chrBinned = c1.getLength() / resolution + 1;
            ListOfDoubleArrays chrNV = new ListOfDoubleArrays(chrBinned);
            for (long k = 0; k < chrNV.getLength(); k++) { // todo optimize a version with system.arraycopy and long
                chrNV.set(k, vector.get(location1 + k));
            }
            location1 += chrNV.getLength();
            normVectorMap.put(c1, new NormalizationVector(norm, c1.getIndex(), zoom.getUnit(), resolution, chrNV));
        }

        return new Pair<>(normVectorMap, expectedValueCalculation);
    }

    public static List<List<ContactRecord>> createWholeGenomeRecords(Dataset dataset, ChromosomeHandler handler,
                                                                     HiCZoom zoom, boolean includeIntra) {
        List<List<ContactRecord>> recordArrayList = new ArrayList<>();
        int addX = 0;
        int addY = 0;
        long maxPos = 0;
        for (Chromosome c1 : handler.getChromosomeArrayWithoutAllByAll()) {
            maxPos += c1.getLength() / zoom.getBinSize() + 1;
        }
        if (maxPos > Integer.MAX_VALUE) {
            System.err.println("Max int size exceeded for genome wide normalization at " + zoom);
        }

        for (Chromosome c1 : handler.getChromosomeArrayWithoutAllByAll()) {
            for (Chromosome c2 : handler.getChromosomeArrayWithoutAllByAll()) {
                if (c1.getIndex() < c2.getIndex() || (c1.equals(c2) && includeIntra)) {
                    MatrixZoomData zd = HiCFileTools.getMatrixZoomData(dataset, c1, c2, zoom);
                    if (zd != null) {
                        for (List<ContactRecord> recordList : zd.getContactRecordList()) {
                            List<ContactRecord> localList = new ArrayList<>();
                            for (ContactRecord cr : recordList) {
                                int binX = cr.getBinX() + addX;
                                int binY = cr.getBinY() + addY;
                                localList.add(new ContactRecord(binX, binY, cr.getCounts()));
                            }
                            recordArrayList.add(localList);
                        }
                    }
                }
                addY += c2.getLength() / zoom.getBinSize() + 1;
            }
            addX += c1.getLength() / zoom.getBinSize() + 1;
            addY = 0;
        }
        return recordArrayList;
    }
}
