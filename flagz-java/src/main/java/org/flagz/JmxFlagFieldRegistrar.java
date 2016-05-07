package org.flagz;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.management.StandardMBean;

/**
 * Registers the flags seen by {@link FlagFieldRegistry} as JMX MBeans.
 */
public class JmxFlagFieldRegistrar {

  @FlagInfo(name = "flagz_jmx_domain", help = "Domain (prefix) used for all Flag JMX objects.")
  private static final Flag<String> jmxDomain = Flagz.valueOf("org.flagz");

  private final FlagFieldRegistry registry;

  public JmxFlagFieldRegistrar(FlagFieldRegistry registry) {
    this.registry = Preconditions.checkNotNull(registry);
  }

  /** Registers FlagField beans with the given MBeanServer server. */
  public void register(MBeanServer server) {
    for (FlagField<?> field : registry.allFields()) {
      FlagFieldMBean<?> delegator = new FlagFieldMBean<>(field);
      delegator.register(server);
    }
  }

  private static class FlagFieldMBean<T> implements FlagMBean {

    private final FlagField<T> delegateFlag;

    FlagFieldMBean(FlagField<T> delegateFlag) {
      this.delegateFlag = Preconditions.checkNotNull(delegateFlag);
    }

    private void register(MBeanServer server) {
      try {
        StandardMBean bean = new StandardMBean(this, FlagMBean.class);
        server.registerMBean(bean, objectName());
      } catch (NotCompliantMBeanException | InstanceAlreadyExistsException
          | MBeanRegistrationException exception) {
        Throwables.propagate(exception);
      }
    }

    private ObjectName objectName() {
      try {
        return new ObjectName(jmxDomain.get(), "name", getName());
      } catch (MalformedObjectNameException exception) {
        Throwables.propagate(exception);
        return null;
      }
    }

    @Override
    public String getName() {
      return delegateFlag.name();
    }

    @Override
    public String getHelp() {
      return delegateFlag.help();
    }

    @Override
    public String getDefaultValue() {
      return delegateFlag.valueString(delegateFlag.defaultValue());
    }

    @Override
    public String getValue() {
      return delegateFlag.valueString(delegateFlag.get());
    }

    @Override
    public String getType() {
      return delegateFlag.fieldType().getTypeName();
    }

    @Override
    public void setValue(String value) {
      try {
        delegateFlag.parseString(value);
      } catch (FlagException exception) {
        throw new IllegalArgumentException("Failed parsing flag " + getName(), exception);
      }
    }
  }

  /** The public interface describing that what will be visible. */
  public interface FlagMBean {

    public String getName();

    public String getHelp();

    public String getDefaultValue();

    public String getValue();

    public String getType();

    public void setValue(String value) throws IllegalArgumentException;

  }
}
