require "json"

package = JSON.parse(File.read(File.join(__dir__, "package.json")))

Pod::Spec.new do |s|
  s.name         = "ArkSecurityManager"
  s.version      = package["version"]
  s.summary      = package["description"]
  s.homepage     = package["homepage"]
  s.license      = package["license"]
  s.authors      = package["author"]

  s.platforms    = { :ios => min_ios_version_supported }
  s.source       = { :git => "https://github.com/hllibrhm01/react-native-ark-security-manager.git", :tag => "#{s.version}" }

  # ObjC bridge + Swift implementation
  s.source_files = "ios/**/*.{h,m,mm,swift}"
  s.private_header_files = "ios/**/*.h"

  # Swift requires frameworks
  s.pod_target_xcconfig = {
    "SWIFT_VERSION"                      => "5.0",
    "DEFINES_MODULE"                     => "YES",
    "SWIFT_OBJC_BRIDGING_HEADER"         => "",
  }

  # ── Local ARK Security Manager iOS SDK ──────────────────────────────────
  # The native iOS SDK is included via a local podspec path.
  # Path: ../native/ios/Modules/ARKSecurityManager
  #
  # The ARKSecurityManager.podspec lives inside that directory.
  # To switch to a remote CocoaPods release, replace with:
  #   s.dependency 'ARKSecurityManager', '0.0.1'
  s.dependency "ARKSecurityManager", :path => "../native/ios/Modules/ARKSecurityManager"

  install_modules_dependencies(s)
end
