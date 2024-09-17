configurations
    .filter { it.name.contains("wasmJs") }
    .onEach {
        it.resolutionStrategy.eachDependency {
            if (requested.group.startsWith("io.ktor") &&
                requested.name.startsWith("ktor-client-")
            ) {
                useVersion("3.0.0-rc-1")
            }
        }
    }
