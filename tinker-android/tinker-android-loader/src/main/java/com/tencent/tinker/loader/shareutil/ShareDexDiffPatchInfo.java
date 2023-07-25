/*
 * Tencent is pleased to support the open source community by making Tinker available.
 *
 * Copyright (C) 2016 THL A29 Limited, a Tencent company. All rights reserved.
 *
 * Licensed under the BSD 3-Clause License (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/BSD-3-Clause
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tencent.tinker.loader.shareutil;

import com.tencent.tinker.loader.TinkerRuntimeException;

import java.util.ArrayList;

/**
 * Created by zhangshaowen on 16/4/11.
 */
public class ShareDexDiffPatchInfo {
    public final String rawName;
    public final String destMd5InDvm;
    public final String destMd5InArt;
    public final String oldDexCrC;
    public final String newOrPatchedDexCrC;

    public final String dexDiffMd5;

    public final String path;

    public final String dexMode;

    public final boolean isJarMode;

    /**
     * if it is jar mode, and the name is end of .dex, we should repackage it with zip, with renaming name.dex.jar
     */
    public final String realName;


    public ShareDexDiffPatchInfo(String name, String path, String destMd5InDvm, String destMd5InArt,
                                 String dexDiffMd5, String oldDexCrc, String newOrPatchedDexCrC, String dexMode) {
        // TODO Auto-generated constructor stub
        this.rawName = name;
        this.path = path;
        this.destMd5InDvm = destMd5InDvm;
        this.destMd5InArt = destMd5InArt;
        this.dexDiffMd5 = dexDiffMd5;
        this.oldDexCrC = oldDexCrc;
        this.newOrPatchedDexCrC = newOrPatchedDexCrC;
        this.dexMode = dexMode;
        if (dexMode.equals(ShareConstants.DEXMODE_JAR)) {
            this.isJarMode = true;
            if (SharePatchFileUtil.isRawDexFile(name)) {
                realName = name + ShareConstants.JAR_SUFFIX;
            } else {
                realName = name;
            }
        } else if (dexMode.equals(ShareConstants.DEXMODE_RAW)) {
            this.isJarMode = false;
            this.realName = name;
        } else {
            throw new TinkerRuntimeException("can't recognize dex mode:" + dexMode);
        }
    }

    /**
     * 解析补丁包中的dex_meta.txt
     * classes.dex,,bed6753ed9218cf252a7c39e620f7127,bed6753ed9218cf252a7c39e620f7127,3b251e269f1cb716eff2402c4ff4d944,3330949685,1459706825,jar
     * classes10.dex,,6817c66b814be0569ea46ee8243b68ee,6817c66b814be0569ea46ee8243b68ee,a146a7e915028e9c506ac482aaab02d9,2056778443,1209957721,jar
     * classes11.dex,,52bee351c87871dd6a3f14de23755d94,52bee351c87871dd6a3f14de23755d94,195861d8247a87470a504258dead1a91,3691397602,777490074,jar
     * classes2.dex,,96d8643b77208944582ffa0332c63e27,96d8643b77208944582ffa0332c63e27,0ff6d09d764925c4848eefa032f16921,752632910,1158437099,jar
     * classes3.dex,,e2dd0cc59fbf299b443d20eb0ea091f4,e2dd0cc59fbf299b443d20eb0ea091f4,fbef7009147f1f05b45dfa56a77a175f,168567500,1761117213,jar
     * classes4.dex,,ab5f8668568612edc047ac2c63ed9112,ab5f8668568612edc047ac2c63ed9112,f5d2b4b27f32adcd9c7ef342a5ad7e52,634967837,2175466630,jar
     * classes5.dex,,fe9b1318c9a6fec8f7cd99f3a652dd81,fe9b1318c9a6fec8f7cd99f3a652dd81,0c7bfba5d938c88db65e724d8804a436,1666121199,995631396,jar
     * classes6.dex,,f89dafd191cbbc38f3f211c9491855ee,f89dafd191cbbc38f3f211c9491855ee,afeaf80bef16f4078713bc80910b024f,962666928,3162761158,jar
     * classes7.dex,,3f08ae4f8c8116163d68ac092ffbe5cc,3f08ae4f8c8116163d68ac092ffbe5cc,ccc9ad54f9a6d82e8d47185ebbad5840,3067628923,3266024778,jar
     * classes8.dex,,1781ad1e91bd462d7fedf60cf2068b06,1781ad1e91bd462d7fedf60cf2068b06,87d51684eebf59645e6bb70f9571e97b,2949640387,3769551028,jar
     * classes9.dex,,84c26b64dc065b54805cd0f6cf8260a9,84c26b64dc065b54805cd0f6cf8260a9,67a0a1bb95a930e0afbf191816bf0641,1834285134,1125935886,jar
     * test.dex,,56900442eb5b7e1de45449d0685e6e00,56900442eb5b7e1de45449d0685e6e00,0,0,0,jar
     * @param meta dex_meta.txt中的内容
     * @param dexList dex信息
     */
    public static void parseDexDiffPatchInfo(String meta, ArrayList<ShareDexDiffPatchInfo> dexList) {
        if (meta == null || meta.length() == 0) {
            return;
        }
        String[] lines = meta.split("\n");//按行拆分
        for (final String line : lines) {
            if (line == null || line.length() <= 0) {
                continue;
            }
            final String[] kv = line.split(",", 8);//按照逗号分隔，最多8个
            if (kv == null || kv.length < 8) {
                continue;
            }

            // key
            final String name = kv[0].trim();//补丁dex名字
            final String path = kv[1].trim();//补丁dex路径，一般为空
            final String destMd5InDvm = kv[2].trim();//合成新dex在dvm中的md5值
            final String destMd5InArt = kv[3].trim();//合成新的dex在art中的md5值，一般和destMd5InDvm的值相同
            final String dexDiffMd5 = kv[4].trim();//补丁包dex文件的md5值
            final String oldDexCrc = kv[5].trim();//基准包中对应dex的crc值（crc为指ZIP条目的CRC32校验和）
            final String newDexCrc = kv[6].trim();//合成新 dex 的 crc 值

            final String dexMode = kv[7].trim();//dex 类型，为 jar 类型

            ShareDexDiffPatchInfo dexInfo = new ShareDexDiffPatchInfo(name, path, destMd5InDvm, destMd5InArt,
                dexDiffMd5, oldDexCrc, newDexCrc, dexMode);
            dexList.add(dexInfo);
        }

    }

    public static boolean checkDexDiffPatchInfo(ShareDexDiffPatchInfo info) {
        if (info == null) {
            return false;
        }
        String name = info.rawName;
        String md5 = (ShareTinkerInternals.isVmArt() ? info.destMd5InArt : info.destMd5InDvm);
        if (name == null || name.length() <= 0 || md5 == null || md5.length() != ShareConstants.MD5_LENGTH) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(rawName);
        sb.append(",");
        sb.append(path);
        sb.append(",");
        sb.append(destMd5InDvm);
        sb.append(",");
        sb.append(destMd5InArt);
        sb.append(",");
        sb.append(oldDexCrC);
        sb.append(",");
        sb.append(newOrPatchedDexCrC);
        sb.append(",");
        sb.append(dexDiffMd5);
        sb.append(",");
        sb.append(dexMode);
        return sb.toString();
    }
}
