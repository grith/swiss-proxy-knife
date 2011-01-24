package grithimport au.org.arcs.auth.shibboleth.StaticCredentialManager;import au.org.arcs.auth.shibboleth.Shibboleth;import au.org.arcs.auth.shibboleth.DummyCredentialManager;import au.org.arcs.auth.shibboleth.StaticIdpObject;import org.vpac.security.light.vomsProxy.VomsProxyimport org.vpac.security.light.certificate.CertificateHelperimport org.vpac.security.light.control.CertificateFiles;import groovy.util.OptionAccessorimport jline.ConsoleReaderimport org.ietf.jgss.GSSCredentialimport org.vpac.security.light.plainProxy.PlainProxyimport org.vpac.security.light.myProxy.MyProxy_lightimport org.vpac.security.light.vomsProxy.VomsProxyCredentialimport org.vpac.security.light.voms.VOManagement.VOManagementimport org.vpac.security.light.voms.VOimport org.vpac.security.light.CredentialHelpersimport org.globus.myproxy.InitParamsimport org.globus.myproxy.MyProxyimport au.org.arcs.jcommons.dependencies.DependencyManager;import org.vpac.security.light.vomsProxy.VomsProxyimport org.globus.gsi.GlobusCredentialimport org.vpac.security.light.utils.HelperMethodsimport org.vpac.security.light.plainProxy.LocalProxyimport java.io.Fileimport org.globus.common.CoGProperties;import au.org.arcs.auth.shibboleth.CredentialManager;import au.org.arcs.auth.shibboleth.IdpObjectimport au.org.arcs.auth.shibboleth.DummyIdpObjectimport grith.gsindl.SLCS;import au.org.arcs.jcommons.utils.ArcsSecurityProvider;import au.org.arcs.jcommons.utils.JythonHelpers;import au.org.arcs.jcommons.dependencies.Dependency;import au.org.arcs.jcommons.dependencies.DependencyManager;import au.org.arcs.jcommons.constants.ArcsEnvironment;
/** * A script that allows you to create a proxy in 3 different ways: normal grid proxy init, myproxy-login, shibboleth. * It also allows you to convert the proxy into a voms proxy if you want to do that and then stores it either on disk or * uploads it to myproxy. *  * @author Markus Binsteiner * */
public class SwissProxyKnife {	public static final String WAYF_URL = "https://slcs1.arcs.org.au/SLCS/login"
	public static final String PROXY_CREATION_MODE_OPTION = "mode"	public static final String SHIBBOLETH_MODE_PARAMETER = "shib-proxy-init"	public static final String CERTIFICATE_MODE_PARAMETER = "grid-proxy-init"	public static final String MYPROXY_MODE_PARAMETER = "myproxy-login"	public static final String SHIBBOLETH_LIST_MODE_PARAMETER = "shib-list"	public static final String USE_LOCAL_MODE_PARAMETER = "load-local-proxy"	public static final String PROXY_OUTPUT_MODE_OPTION = "action"	public static final String LOCAL_OUTPUT_MODE_PARAMETER = "store-local"	public static final String MYPROXY_OUTPUT_MODE_PARAMETER = "myproxy-upload"	public static final String VOMS_LIST_GROUPS = "list-groups"	public static final String INFO_ACTION_PARAMETER = "info"	public static final String VOMS_PROXY_OPTION = "voms"	public static final String SHIBBOLETH_IDP_PARAMETER = "idp"	public static final String USERNAME_PARAMETER = "username"	public static final String MYPROXY_USERNAME_PARAMETER = "myproxy_username"	public static final String MYPROXY_PROXYNAME_PARAMETER = "myproxy_proxyname"	public static final String MYPROXY_SERVER_PARAMETER = "server"	public static final String MYPROXY_PORT_PARAMETER = "port"	public static final String LIFETIME_PARAMETER = "lifetime"	public static final VOMS_GROUP_PARAMETER = "voms_group"	public final static String VERBOSE_OPTION = "verbose"	public final static String DEBUG_OPTION = "debug"
	private OptionAccessor opt	CliBuilder cl	private int lifetime = 24	private String username = null	private String action = null	private String myProxyUsername	private String myProxyServer = "myproxy2.arcs.org.au"	private int myProxyPort = 443	private String vomsGroup = null
	GSSCredential cred
	public SwissProxyKnife(String[] args) {
		cl = new CliBuilder()
		cl.h(longOpt:'help', 'usage information')
		cl.m(longOpt:PROXY_CREATION_MODE_OPTION, argName:'mode', args:1, required:true, 'The mode to create the proxy. Possible arguments: '+USE_LOCAL_MODE_PARAMETER+', '+CERTIFICATE_MODE_PARAMETER+', '+MYPROXY_MODE_PARAMETER+', '+SHIBBOLETH_MODE_PARAMETER+', '+SHIBBOLETH_LIST_MODE_PARAMETER)
		cl.l(longOpt:LIFETIME_PARAMETER, argName:'lifetime in hours', args:1, required:false, 'The lifetime of the proxy in hours. Default: 24')
		cl.u(longOpt:USERNAME_PARAMETER, argName:'username', args:1, required:false, 'The myproxy- or shibboleth-username used to get delegated proxy/shibboleth certificate')
		cl.i(longOpt:SHIBBOLETH_IDP_PARAMETER, argName:'idp name', args:1, required:false, 'The name of the idp to connect to')		cl.d(longOpt:MYPROXY_USERNAME_PARAMETER, argName:'myproxy username', args:1, required:false, 'The username to user when delegating the proxy to MyProxy')
		cl.s(longOpt:MYPROXY_PROXYNAME_PARAMETER, argName:'myproxy server', args:1, required:false, 'The myproxy server')
		cl.p(longOpt:MYPROXY_PORT_PARAMETER, argName:'myproxy port', args:1, required:false, 'The myproxy port')		cl.v(longOpt:VOMS_GROUP_PARAMETER, argName:'voms group', args:1, required:false, 'The voms group you want this proxy to represent')
		cl.a(longOpt:PROXY_OUTPUT_MODE_OPTION, argName:'action', args:1, required:false, 'What to do with the proxy. Use one of these options: '+LOCAL_OUTPUT_MODE_PARAMETER+', '+MYPROXY_OUTPUT_MODE_PARAMETER+', '+INFO_ACTION_PARAMETER+', '+VOMS_LIST_GROUPS)
		opt = cl.parse(args)
		CertificateFiles.copyCACerts(false);		CoGProperties.getDefault().setProperty(CoGProperties.ENFORCE_SIGNING_POLICY, "false");		if (!opt) {			// because the parse failed, the usage will be shown automatically			println "\nInvalid command line, exiting..."		} else if (opt.h) {			cl.usage()		} else {			// do some more parsing			if ( opt.s ) {				myProxyServer = opt.s			}			if ( opt.p ) {				myProxyPort = Integer.parseInt(opt.p)			}			if ( opt.v ) {				vomsGroup = opt.v			}			if ( opt.l ) {				lifetime  = Integer.parseInt(opt.l)			}			if ( opt.u ) {				username = opt.u			}			if ( opt.a ) {				action = opt.a				if ( action != LOCAL_OUTPUT_MODE_PARAMETER && action != MYPROXY_OUTPUT_MODE_PARAMETER && action != VOMS_LIST_GROUPS && action != INFO_ACTION_PARAMETER ) {					println("Unsupported action. Exiting...")					cl.usage()					System.exit(1)				}				if ( action == MYPROXY_OUTPUT_MODE_PARAMETER && !opt.d) {					println("No myproxy username for delegation of proxy specified. Exiting...")					System.exit(1)				}				myProxyUsername = opt.d			}			// create proxy			switch ( opt.m ) {				case CERTIFICATE_MODE_PARAMETER:					if ( ! action ) {						println("No action specified. Exiting...")						cl.usage()						System.exit(1)					}					cred = createLocalCertificate()					break				case MYPROXY_MODE_PARAMETER:					if ( ! action ) {						println("No action specified. Exiting...")						cl.usage()						System.exit(1)					}					if ( ! username ) {						println("No username provided for myproxy credential retrieval from MyProxy.")						cl.usage()						System.exit(1)					}					cred = createMyProxyCredential()					break				case SHIBBOLETH_MODE_PARAMETER:					if ( ! action ) {						println("No action specified. Exiting...")						cl.usage()						System.exit(1)					}					if ( ! username ) {						println("No username provided for shibboleth credential retrieval from MyProxy.")						cl.usage()						System.exit(1)					}					if ( ! opt.i ) {						println("No idp specified. Exiting...")						System.exit(1)					}					cred = createShibCredential()					break				case USE_LOCAL_MODE_PARAMETER:					try {						cred = LocalProxy.loadGSSCredential()					} catch (Exception e) {						println('Could not load local proxy: '+e.getLocalizedMessage())						System.exit(1)					}					break				case SHIBBOLETH_LIST_MODE_PARAMETER:					printAllIdps()					System.exit(0)				default:					println("Unsupported mode parameter")					System.exit(1)			}			if ( ! cred ) {				println("Certificate could not be created for unknown reason. Exiting...")				System.exit(1)			}			// convert to voms proxy if requested			if ( vomsGroup ) {				VomsProxyCredential vomsProxy = createVomsCredential()				cred = CredentialHelpers.wrapGlobusCredential(vomsProxy.getVomsProxy())			}			// do stuff with proxy			switch ( opt.a ) {				case LOCAL_OUTPUT_MODE_PARAMETER:					writeToDisk()					System.exit(0)				case MYPROXY_OUTPUT_MODE_PARAMETER:					uploadToMyProxy()					System.exit(0)				case VOMS_LIST_GROUPS:					Set<String> allVomsGroups = getAllVomsGroups()					for ( String group in allVomsGroups ) {						println(group)					}					System.exit(0)				case INFO_ACTION_PARAMETER:					printInfo()					System.exit(0)				default:					println("Unsupported action parameter")					System.exit(1)			}		}	}	private void printAllIdps() {		IdpObject idpObject = new DummyIdpObject();		CredentialManager cm = new DummyCredentialManager()		Shibboleth shib = new Shibboleth(idpObject, cm);		shib.openurl(WAYF_URL)		for ( idp in idpObject.getIdps() ) {			println(idp)		}		System.exit(0);	}	private void printInfo() {		GlobusCredential proxy = CredentialHelpers.unwrapGlobusCredential(cred)		println('subject\t\t:  '+proxy.getSubject())		println('issuer\t\t:  '+proxy.getIssuer())		println('identity\t:  '+proxy.getIdentity())		println('type\t\t:  don\'t know. if you know how to figure that out, contact makkus+grith@gmail.com')		println('strength\t:  '+proxy.getStrength())		println('timeleft\t:  '+HelperMethods.getFormatedTime(proxy.getTimeLeft()))		println()		try{			VomsProxy vomsProxy = new VomsProxy(proxy)			for ( String line in vomsProxy.getVomsInfo() ) {				println(line)			}		} catch (Exception e) {			println('No voms attribute certificate attached')		}	}	private void writeToDisk() {		try {			CredentialHelpers.writeToDisk(cred, new File(LocalProxy.PROXY_FILE))			println('Proxy stored to: '+LocalProxy.PROXY_FILE)		} catch (IOException e) {			println('Could not write proxy to disk. Exiting...')			System.exit(1)		}	}	private void uploadToMyProxy() {		InitParams params = MyProxy_light.prepareProxyParameters(myProxyUsername, null, null, null, null, lifetime*3600)		// reading myproxy passphrase		char[] password = null		try {			ConsoleReader consoleReader = new ConsoleReader()			password = consoleReader.readLine('Please provide the myproxy password to delegate the proxy: ', "x".charAt(0)).toCharArray()		} catch (e) {			println('Could nor read from standard input. Exiting...')			System.exit(1)		}		MyProxy myProxy = new MyProxy(myProxyServer, myProxyPort);		try {			MyProxy_light.init(myProxy, cred, params, password)		} catch (Exception e) {			println('Could not delegate proxy: '+e.getLocalizedMessage())			System.exit(1)		}		println('MyProxy delegation successful')	}	private Set<String> getAllVomsGroups() {		Set<String> allVomsGroups = new TreeSet<String>()		VO vo		Map<VO, Map<String, Set<String>>> allInfo = VOManagement.getAllInformationAboutUser(cred)		for ( VO voTemp in allInfo.keySet() ) {			for ( String group in allInfo.get(voTemp).keySet() ) {				allVomsGroups.add(group)			}		}		return allVomsGroups	}	private VomsProxyCredential createVomsCredential() {		// find the proper VO for the group		VO vo		Map<VO, Map<String, Set<String>>> allInfo = VOManagement.getAllInformationAboutUser(cred)		for ( VO voTemp in allInfo.keySet() ) {			for ( String group in allInfo.get(voTemp).keySet() ) {				if ( vomsGroup == group ) {					vo = voTemp					break				}			}			if ( vo ) {				break			}		}		if ( ! vo ) {			println("Can't find VO for group: "+vomsGroup+". Exiting...")			System.exit(1)		}		VomsProxyCredential vomsGssCred = null;		try {			vomsGssCred = new VomsProxyCredential(CredentialHelpers.unwrapGlobusCredential(cred), vo, "G"+vomsGroup, null);		} catch (Exception e) {			println("Could not create voms proxy: "+e.getLocalizedMessage())			System.exit(1)		}		return vomsGssCred	}	private GSSCredential createLocalCertificate() {		//check prerequisites		if ( ! CertificateHelper.globusCredentialsReady() ) {			println("Certificate or private key not in default location or not readable. Exiting...")			System.exit(1)		}		char[] password = null		try {			ConsoleReader consoleReader = new ConsoleReader()			password = consoleReader.readLine('Please provide your private key passphrase: ', "x".charAt(0)).toCharArray()		} catch (e) {			println("Could nor read from standard input. Exiting...")			System.exit(1)		}		GSSCredential cred		try {			cred = PlainProxy.init(password, lifetime)		} catch (Exception e) {			println("Could not init proxy: "+e.getLocalizedMessage()+". Exiting...")			System.exit(1)		}		return cred	}	private GSSCredential createMyProxyCredential() {		char[] password = null		try {			ConsoleReader consoleReader = new ConsoleReader()			password = consoleReader.readLine('Please provide your myproxy password: ', "x".charAt(0)).toCharArray()		} catch (e) {			println("Could nor read from standard input. Exiting...")			System.exit(1)		}		GSSCredential cred		try {			cred = MyProxy_light.getDelegation(myProxyServer, myProxyPort, username, password, lifetime*3600)		} catch (Exception e) {			println("Could not init proxy: "+e.getLocalizedMessage()+". Exiting...")			System.exit(1)		}		return cred	}	private GSSCredential createShibCredential() {		IdpObject idpObject = new StaticIdpObject(opt.i);		char[] password = null		try {			ConsoleReader consoleReader = new ConsoleReader()			password = consoleReader.readLine('Please provide your institution password: ', "x".charAt(0)).toCharArray()		} catch (e) {			println("Could nor read from standard input. Exiting...")			System.exit(1)		}		try {			CredentialManager credentialManager = new StaticCredentialManager(username, password)			Shibboleth shibboleth = new Shibboleth(idpObject, credentialManager);			shibboleth.openurl(WAYF_URL)			SLCS slcs = new SLCS(shibboleth);			slcs.shibLoginComplete(shibboleth.getResponse())			GSSCredential proxy = PlainProxy.init(slcs.getCertificate(), slcs.getPrivateKey(), lifetime);			return proxy			println(slcs.getCertificate().getIssuerDN().getName())		} catch (Exception e) {			println("Could not get slcs certificate: "+e.getLocalizedMessage())			System.exit(1)		}		//		List<IDP> idps = getAllIdps()		//		//		IDP selectedIdp		//		//		for ( IDP idp in idps ) {		//			if ( opt.i == idp.name ) {		//				selectedIdp = idp		//				break		//			}		//		}		//		//		if ( ! selectedIdp ) {		//			println("Could not find idp \""+opt.i+"\". Exiting...")		//			System.exit(1)		//		}		//		//		println("debug: Found correct idp: "+selectedIdp)		//		//		char[] password = null		//		//		try {		//			ConsoleReader consoleReader = new ConsoleReader()		//			password = consoleReader.readLine('Please provide your institution password: ', "x".charAt(0)).toCharArray()		//		} catch (e) {		//			println("Could nor read from standard input. Exiting...")		//			System.exit(1)		//		}		//		//		println("debug: Password read successfully")		//		//		GSSCredential credential		//		try {		//			credential = client.slcsLogin(selectedIdp, username, password);		//		} catch (Exception e) {		//			println('The exception:')		//			e.printStackTrace()		//			if ( e.getCause() != null ) {		//				println('The cause for the exception:')		//				e.getCause().printStackTrace()		//			}		//		}		//		//		//		return credential		return null;	}
	public static void main(String[] args) {		java.security.Security.addProvider(new ArcsSecurityProvider());		java.security.Security.setProperty("ssl.TrustManagerFactory.algorithm",				"TrustAllCertificates");		JythonHelpers.setJythonCachedir();		Map<Dependency, String> dependencies = new HashMap<Dependency, String>();		dependencies.put(Dependency.BOUNCYCASTLE, "jdk15-143");		dependencies.put(Dependency.ARCSGSI, "1.1");		DependencyManager.addDependencies(dependencies, ArcsEnvironment.getArcsCommonJavaLibDirectory());		Shibboleth.initDefaultSecurityProvider();

		SwissProxyKnife spk = new SwissProxyKnife(args)	}}
