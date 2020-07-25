package org.example.registry;

public class ServiceDetail {

    public static final String REGISTER_ROOT_PATH = "/myRPC";

    String interfaceName;

    public ServiceDetail() {}

    public ServiceDetail(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }
}
