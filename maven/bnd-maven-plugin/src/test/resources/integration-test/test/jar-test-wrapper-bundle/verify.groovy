import java.util.jar.*;

def bsn = 'test.wrapper.bundle'
def moduleDir = 'jar-test-wrapper-bundle'

// Check the bundles exist!
File wrapper_bundle = new File(basedir, "${moduleDir}/target/${moduleDir}-0.0.1-BUILD-SNAPSHOT.jar")
assert wrapper_bundle.isFile()

// Load manifests
JarFile wrapper_jar = new JarFile(wrapper_bundle)
Attributes wrapper_manifest = wrapper_jar.getManifest().getMainAttributes()

// Basic manifest check
assert wrapper_manifest.getValue('Bundle-SymbolicName') == bsn
assert wrapper_manifest.getValue('Bundle-Name') == moduleDir
assert wrapper_manifest.getValue('Bundle-Version') == '0.0.1.BUILD-SNAPSHOT'
assert wrapper_manifest.getValue('Bundle-ClassPath') == '.,lib/osgi.annotation.jar'
assert wrapper_manifest.getValue('Bnd-LastModified') == null

// Check inheritance of properties in bnd.bnd from the parent project
assert wrapper_manifest.getValue('X-ParentProjectProperty') == 'overridden'

// Check -include of bnd files
assert wrapper_manifest.getValue('X-IncludedParentProjectProperty') == 'Included via -include in parent bnd.bnd file'
assert wrapper_manifest.getValue('X-IncludedProperty') == 'Included via -include in project bnd.bnd file'

// Check bnd properties
assert wrapper_manifest.getValue('Project-Name') == moduleDir
assert wrapper_manifest.getValue('Project-Dir') == new File(basedir, moduleDir).absolutePath.replace(File.separatorChar, '/' as char)
assert wrapper_manifest.getValue('Project-Output') == new File(basedir, "${moduleDir}/target").absolutePath
assert wrapper_manifest.getValue('Project-Buildpath')
//assert !wrapper_manifest.getValue('Project-Sourcepath')
assert wrapper_manifest.getValue('Parent-Here') == new File(basedir, 'jar-parent').absolutePath.replace(File.separatorChar, '/' as char)

// Check contents
assert wrapper_jar.getEntry('org/example/api/') != null
assert wrapper_jar.getEntry('org/example/types/') != null
assert wrapper_jar.getEntry('lib/osgi.annotation.jar') != null
