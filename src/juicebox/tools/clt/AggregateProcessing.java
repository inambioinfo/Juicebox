/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2011-2018 Broad Institute, Aiden Lab
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

package juicebox.tools.clt;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created for testing multiple CLTs at once
 * Basically scratch space
 */
class AggregateProcessing {


    public static void main(String[] argv) {


        String[] ll51231123 = {"compare", "0", "hg19",
                "https://hicfiles.s3.amazonaws.com/hiseq/gm12878/in-situ/combined_peaks_with_motifs.txt",
                //"/Users/muhammadsaadshamim/Desktop/MBR19_loops.txt",
                "/Users/muhammadsaadshamim/Desktop/result_hiccups/merged_loops.bedpe",
                "/Users/muhammadsaadshamim/Desktop/result_100kb_hiccups_compare.bedpe"};

        // GSE63525_GM12878_insitu_primary_30.hic

        // gm12878_intra_nofrag_30.hic

        ll51231123 = new String[]{"dice",
                "/Volumes/AidenLabWD7/Backup/AidenLab/LocalFiles/gm12878/GSE63525_GM12878_insitu_replicate_30.hic",
                "/Users/muhammad/Desktop/new_100k_curse_delta_replicate"};

        //HiCTools.main(ll51231123);

        ll51231123 = new String[]{"dice",
                "/Volumes/AidenLabWD7/Backup/AidenLab/LocalFiles/gm12878/GSE63525_GM12878_insitu_primary_30.hic",
                "/Users/muhammad/Desktop/new_100k_oddeven_curse_echo_primary"};

        ll51231123 = new String[]{"dice",
                "/Volumes/AidenLabWD7/Backup/AidenLab/LocalFiles/gm12878/gm12878_intra_nofrag_30.hic+/Volumes/AidenLabWD7/Backup/AidenLab/LocalFiles/gm12878/gm12878_intra_nofrag_30.hic+/Volumes/AidenLabWD7/Backup/AidenLab/LocalFiles/gm12878/gm12878_intra_nofrag_30.hic",
                //"/Volumes/AidenLabWD7/Backup/AidenLab/LocalFiles/gm12878/GSE63525_GM12878_insitu_primary_30.hic+/Volumes/AidenLabWD7/Backup/AidenLab/LocalFiles/gm12878/GSE63525_GM12878_insitu_primary_30.hic",
                "/Users/muhammad/Desktop/ECHO6/gm12878_self_50k"};


        ll51231123 = new String[]{"dice",
                //"/Volumes/AidenLabWD7/Backup/AidenLab/LocalFiles/gm12878/GSE63525_GM12878_insitu_primary_30.hic+/Volumes/AidenLabWD7/Backup/AidenLab/LocalFiles/gm12878/GSE63525_GM12878_insitu_primary_30.hic+/Volumes/AidenLabWD7/Backup/AidenLab/LocalFiles/gm12878/GSE63525_GM12878_insitu_primary_30.hic",
                //"/Volumes/AidenLabWD7/Backup/AidenLab/LocalFiles/gm12878/GSE63525_GM12878_insitu_primary_30.hic+/Volumes/AidenLabWD7/Backup/AidenLab/LocalFiles/gm12878/GSE63525_GM12878_insitu_replicate_30.hic",
                "/Volumes/AidenLabWD7/Backup/AidenLab/LocalFiles/gm12878/GSE63525_GM12878_insitu_primary_30.hic+" +
                        "/Volumes/AidenLabWD7/Backup/AidenLab/LocalFiles/gm12878/GSE63525_GM12878_insitu_replicate_30.hic+" +
                        "/Volumes/AidenLabWD7/Backup/AidenLab/LocalFiles/gm12878/GSE63525_GM12878_insitu_DpnII_combined_30.hic+" +
                        "/Volumes/AidenLabWD7/Backup/AidenLab/LocalFiles/gm12878/gm12878_intra_nofrag_30.hic",
                "/Users/muhammad/Desktop/ECHO6/gm12878s"};


        ll51231123 = new String[]{"dice",
                //"/Volumes/AidenLabWD7/Backup/AidenLab/LocalFiles/gm12878/GSE63525_GM12878_insitu_primary_30.hic+/Volumes/AidenLabWD7/Backup/AidenLab/LocalFiles/gm12878/GSE63525_GM12878_insitu_primary_30.hic+/Volumes/AidenLabWD7/Backup/AidenLab/LocalFiles/gm12878/GSE63525_GM12878_insitu_primary_30.hic",
                //"/Volumes/AidenLabWD7/Backup/AidenLab/LocalFiles/gm12878/GSE63525_GM12878_insitu_primary_30.hic+/Volumes/AidenLabWD7/Backup/AidenLab/LocalFiles/gm12878/GSE63525_GM12878_insitu_replicate_30.hic",
                "/Volumes/AidenLabWD7/Backup/AidenLab/LocalFiles/gm12878/GSE63525_GM12878_diploid_maternal.hic+" +
                        "/Volumes/AidenLabWD7/Backup/AidenLab/LocalFiles/gm12878/GSE63525_GM12878_diploid_paternal.hic",
                "/Users/muhammad/Desktop/ECHO5/echo5_maternalVSpaternal_100k"};

        ll51231123 = new String[]{"dice",
                //"/Volumes/AidenLabWD7/Backup/AidenLab/LocalFiles/gm12878/GSE63525_GM12878_insitu_primary_30.hic+/Volumes/AidenLabWD7/Backup/AidenLab/LocalFiles/gm12878/GSE63525_GM12878_insitu_primary_30.hic+/Volumes/AidenLabWD7/Backup/AidenLab/LocalFiles/gm12878/GSE63525_GM12878_insitu_primary_30.hic",
                //"/Volumes/AidenLabWD7/Backup/AidenLab/LocalFiles/gm12878/GSE63525_GM12878_insitu_primary_30.hic+/Volumes/AidenLabWD7/Backup/AidenLab/LocalFiles/gm12878/GSE63525_GM12878_insitu_replicate_30.hic",
                "/Volumes/AidenLabWD7/Backup/AidenLab/LocalFiles/degron/6hrtreat_nosync_combined.hic+" +
                        "/Volumes/AidenLabWD7/Backup/AidenLab/LocalFiles/degron/notreat_nosync_combined.hic",
                "/Users/muhammad/Desktop/ECHO6/degron"};

        //HiCTools.main(ll51231123);

        ll51231123 = new String[]{"dice",
                //"/Volumes/AidenLabWD7/Backup/AidenLab/LocalFiles/gm12878/GSE63525_GM12878_insitu_primary_30.hic+/Volumes/AidenLabWD7/Backup/AidenLab/LocalFiles/gm12878/GSE63525_GM12878_insitu_primary_30.hic+/Volumes/AidenLabWD7/Backup/AidenLab/LocalFiles/gm12878/GSE63525_GM12878_insitu_primary_30.hic",
                //"/Volumes/AidenLabWD7/Backup/AidenLab/LocalFiles/gm12878/GSE63525_GM12878_insitu_primary_30.hic+/Volumes/AidenLabWD7/Backup/AidenLab/LocalFiles/gm12878/GSE63525_GM12878_insitu_replicate_30.hic",
                "/Volumes/AidenLabWD7/Backup/AidenLab/LocalFiles/collins/map1_30.hic+" +
                        "/Volumes/AidenLabWD7/Backup/AidenLab/LocalFiles/collins/map2_30.hic",
                "/Users/muhammad/Desktop/ECHO5/collins_50k"};

        ll51231123 = new String[]{"dice",
                "/Volumes/AidenLabWD7/Backup/AidenLab/LocalFiles/goodell/HIC1255_tcell_30.hic+" +
                        "/Volumes/AidenLabWD7/Backup/AidenLab/LocalFiles/goodell/HIC1258_3lanes_30.hic+" +
                        "/Volumes/AidenLabWD7/Backup/AidenLab/LocalFiles/goodell/day7_HIC1528_30.hic",
                "/Users/muhammad/Desktop/ECHO6/goodell"};

        ll51231123 = new String[]{"dice",
                "/Volumes/AidenLabWD7/Backup/AidenLab/LocalFiles/gm12878/gm12878_intra_nofrag_30.hic+" +
                        "/Volumes/AidenLabWD7/Backup/AidenLab/LocalFiles/imr90_30_nf/imr90_intra_nofrag_30.hic+" +
                        "/Volumes/AidenLabWD7/Backup/AidenLab/LocalFiles/k562/combined_30.hic",
                "/Users/muhammad/Desktop/ECHO6/gm12878_imr90_k562_trio"};


        ll51231123 = new String[]{"dice",
                "/Volumes/AidenLabWD7/Backup/AidenLab/LocalFiles/Marianna/GM_38all_mega.hic+" +
                        "/Volumes/AidenLabWD7/Backup/AidenLab/LocalFiles/Marianna/GM_39all_mega.hic+" +
                        "/Volumes/AidenLabWD7/Backup/AidenLab/LocalFiles/Marianna/GM40all_mega.hic",
                "/Users/muhammad/Desktop/ECHO6/marianna"};

        ll51231123 = new String[]{"dice",
                "/Volumes/AidenLabWD7/Backup/AidenLab/LocalFiles/gm12878/GSE63525_GM12878_insitu_primary_30.hic+" +
                        "/Volumes/AidenLabWD7/Backup/AidenLab/LocalFiles/gm12878/GSE63525_GM12878_insitu_replicate_30.hic",
                "/Users/muhammad/Desktop/ECHO6/pure_gm12878_50k"};
        // HiCTools.main(ll51231123);
        ll51231123 = new String[]{"dice",
                "/Volumes/AidenLabWD7/Backup/AidenLab/LocalFiles/k562/combined_30.hic+" +
                        "/Volumes/AidenLabWD7/Backup/AidenLab/LocalFiles/k562/combined_30.hic",
                "/Users/muhammad/Desktop/ECHO6/pure_k562_100k"};

        ll51231123 = new String[]{"pre", "-f",
                "/Users/muhammad/Desktop/testjuicer/reerrorwhenrunningjuicerpipeline/hg19_MboI.txt",
                "-s", "/Users/muhammad/Desktop/testjuicer/reerrorwhenrunningjuicerpipeline/inter.txt",
                "-g", "/Users/muhammad/Desktop/testjuicer/reerrorwhenrunningjuicerpipeline/inter_hists.m",
                "-q", "1", "/Users/muhammad/Desktop/testjuicer/reerrorwhenrunningjuicerpipeline/merged_nodups.txt",
                "/Users/muhammad/Desktop/testjuicer/reerrorwhenrunningjuicerpipeline/inter.hic",
                "/Users/muhammad/Desktop/testjuicer/reerrorwhenrunningjuicerpipeline/hg19.chrom.sizes"};


        //HiCTools.main(ll51231123);






    }

    private static void writeMergedNoDupsFromTimeSeq(String seqPath, String newPath) {
        List<Integer[]> listPositions = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(seqPath))) {
            for (String line; (line = br.readLine()) != null; ) {
                String[] parts = line.split(",");
                listPositions.add(new Integer[]{Integer.parseInt(parts[0]), Integer.parseInt(parts[1])});
            }
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }


        try {
            PrintWriter p0 = new PrintWriter(new FileWriter(newPath));
            for (int i = 0; i < listPositions.size(); i++) {
                Integer[] pos_xy_1 = listPositions.get(i);
                for (int j = i; j < listPositions.size(); j++) {
                    Integer[] pos_xy_2 = listPositions.get(j);
                    double value = 1. / Math.max(1, Math.sqrt((pos_xy_1[0] - pos_xy_2[0]) ^ 2 + (pos_xy_1[1] - pos_xy_2[1]) ^ 2));
                    float conv_val = (float) value;
                    if (!Float.isNaN(conv_val) && conv_val > 0) {
                        p0.println("0 art " + i + " 0 16 art " + j + " 1 " + conv_val);
                    }
                }
            }
            p0.close();
        } catch (IOException ignored) {
            ignored.printStackTrace();
        }
    }
}