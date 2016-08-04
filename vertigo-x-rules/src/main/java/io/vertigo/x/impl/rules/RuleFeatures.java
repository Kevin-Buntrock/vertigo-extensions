package io.vertigo.x.impl.rules;

import io.vertigo.app.config.Features;
import io.vertigo.commons.impl.script.ScriptManagerImpl;
import io.vertigo.commons.plugins.script.janino.JaninoExpressionEvaluatorPlugin;
import io.vertigo.commons.script.ScriptManager;
import io.vertigo.x.rules.RuleManager;

/**
 * Defines the 'workflow' extension
 * @author xdurand
 */
public final class RuleFeatures extends Features {

	/**
	 * Constructor.
	 */
	public RuleFeatures() {
		super("x-rules");
	}

	/** {@inheritDoc} */
	@Override
	protected void setUp() {
		getModuleConfigBuilder()
				.addDefinitionProvider(RuleProvider.class)
				.addComponent(RuleManager.class, RuleManagerImpl.class)
				.addComponent(ScriptManager.class, ScriptManagerImpl.class)
				/*.addComponent(AccountManager.class, AccountManagerImpl.class)
				.addComponent(FileManager.class, FileManagerImpl.class)
				.beginComponent(VSecurityManager.class, VSecurityManagerImpl.class)
				.addParam("userSessionClassName", "account")
				.endComponent()
				/*.addComponent(DaemonManager.class, DaemonManagerImpl.class)
				.beginComponent(LocaleManager.class, LocaleManagerImpl.class)
				.addParam("locales", "fr")
				.endComponent()*/
				.addPlugin(JaninoExpressionEvaluatorPlugin.class);
	}

}
