package com.autojump.plugin


public class ScanSetting {

    public static final String Plugin_Name = "AutoJumpTransform"

    public static final String General_To_Class_Name = "com/zoom/api/DistributionCenter"

    public static final String General_To_Class_File_Name = General_To_Class_Name + ".class"

    public static final String Interface_Package_Name = "com/zoom/compile/"

    public static final String General_To_Method_Name = "loadDispatchMap"

    public static final String Auto_Class_Package_Name = "com/zoom/dispatch"

    public static final String Auto_Class_Method_Name = "registerByPlugin"

    String interfaceName = ""

    ArrayList<String> classList = new ArrayList<>()

    ScanSetting(String interfaceName) {
        this.interfaceName = Interface_Package_Name + interfaceName
    }
}