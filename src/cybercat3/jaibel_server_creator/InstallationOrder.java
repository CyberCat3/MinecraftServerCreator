package cybercat3.jaibel_server_creator;

public class InstallationOrder {
    private ServerType serverType;
    private String version;
    private boolean usePreexistingWorld;
    private String pathToWorld;
    private String pathToTarget;

    @Override
    public String toString() {
        return "InstallationOrder{" +
                "serverType=" + serverType +
                ", version='" + version + '\'' +
                ", usePreexistingWorld=" + usePreexistingWorld +
                ", pathToWorld='" + pathToWorld + '\'' +
                ", pathToTarget='" + pathToTarget + '\'' +
                '}';
    }

    public ServerType getServerType() {
        return serverType;
    }

    public void setServerType(ServerType serverType) {
        this.serverType = serverType;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public boolean usePreexistingWorld() {
        return usePreexistingWorld;
    }

    public void usePreexistingWorld(boolean usePreexistingWorld) {
        this.usePreexistingWorld = usePreexistingWorld;
    }

    public String getPathToWorld() {
        return pathToWorld;
    }

    public void setPathToWorld(String pathToWorld) {
        this.pathToWorld = pathToWorld;
    }

    public String getPathToTarget() {
        return pathToTarget;
    }

    public void setPathToTarget(String pathToTarget) {
        this.pathToTarget = pathToTarget;
    }
}
