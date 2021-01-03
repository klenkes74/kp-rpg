/**
 * @author klenkes74
 * @since 1.0.0 2021-01-03
 */
module de.kaiserpfalzedv.rpg.torg {
    requires de.kaiserpfalzedv.rpg.core;
    requires org.slf4j;
    requires jakarta.inject.api;
    requires jakarta.enterprise.cdi.api;

    exports de.kaiserpfalzedv.rpg.torg.dice;
    exports de.kaiserpfalzedv.rpg.torg;
}