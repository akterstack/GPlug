import com.jroadie.gplug.engine.plugin.GrailsPluginBase
import com.jroadie.gplug.engine.plugin.PluginMeta

class BoilerplateGrailsPlugin extends GrailsPluginBase {
    String version = "1.0.0";
    {
        grailsVersion = "2.4.0 > *"
        title = "Boilerplate" // Headline display name of the plugin
        author = "Akter Hossain"
        authorEmail = "akter@bitmascot.com"
        description = '''Description'''
        documentation = "http://grails.org/plugin/gplug-boilerplate";
        _plugin = new PluginMeta(identifier: "boilerplate", name: title, loader: BoilerplateGrailsPlugin.classLoader);
        hooks = []
    }
}