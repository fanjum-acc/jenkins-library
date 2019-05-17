import jenkins.model.Jenkins

def stepCallMapping = new groovy.json.JsonSlurper().parseText(new File(System.getenv()['calls']).text)

def stepPluginMapping = [:]

println "[INFO] Resolving plugins ..."

for(def step in stepCallMapping) {
    def resolvedPlugins = [:]
    for(def call in step.value) {
        def resolvedPlugin = resolvePlugin(call)
        if (! resolvedPlugin) resolvedPlugin = 'UNIDENTIFIED'
        if(resolvedPlugins[resolvedPlugin] == null)
            resolvedPlugins[resolvedPlugin] = (Set)[]
            resolvedPlugins[resolvedPlugin] << call
            stepPluginMapping.put(step.key,resolvedPlugins)
    }
}

def result = System.getenv()['result']
new File(result).write(new groovy.json.JsonOutput().toJson(stepPluginMapping))

println "[INFO] plugins resolved. Result: ${result}."


def resolvePlugin(call) {

    def plugins = Jenkins.get().pluginManager.getPlugins()

    def s = new org.jenkinsci.plugins.workflow.cps.Snippetizer()

    def pDescs = s.getQuasiDescriptors(false)


    for(def pd in pDescs) {
        if(pd.getSymbol() == call)
            return  pd.real.plugin?.shortName
    }
    return null
}
