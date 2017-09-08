package com.meituan.robust.mapping;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by hedex on 17/2/23.
 */

public class ProGuardMappingUtils {
    public static Map<String, ClassMapping> classMappings = new LinkedHashMap<String, ClassMapping>();
    private ProGuardMappingUtils() {

    }
    public static void main(String[] args){
        String mappingPath = "/Users/hedingxu/robust-github/Robust/app/robust/mapping.txt";
        handleMappingFile(mappingPath);
    }

    public static void handleMappingFile(String mappingPath) {
        File applyMapping = new File(mappingPath);

        MappingReader reader = new MappingReader(applyMapping);

        MappingProcessor keeper = new MappingContainer(classMappings);
        try {
            reader.pump(keeper);
        } catch (IOException e) {
            e.printStackTrace();
        }
//        for (String className : classMappings.keySet()) {
//            if (className.contains("com.meituan.sample.test.BadClass")){
//                ClassMapping classMapping = classMappings.get(className);
//                System.out.print(classMapping.toString());
//            }
//
//        }
    }

}
