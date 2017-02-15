/*
 *     wac-core
 *     Copyright (C) 2016 Martijn Heil
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package tk.martijn_heil.wac_core;


import org.apache.commons.lang.exception.ExceptionUtils;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationVersion;

public class HackyClass
{
    public static boolean doStuff(String dbUrl, String dbUsername, String dbPassword, ClassLoader pluginLoader) {
        Flyway flyway = new Flyway();
        flyway.setClassLoader(pluginLoader);
        flyway.setDataSource(dbUrl, dbUsername, dbPassword);
        flyway.setBaselineOnMigrate(true);
        flyway.setBaselineVersion(MigrationVersion.fromVersion("0"));

        try {
            flyway.migrate();
        } catch (FlywayException ex) {
            WacCore.logger.severe(ex.getMessage());
            WacCore.logger.fine(ExceptionUtils.getFullStackTrace(ex));
            WacCore.logger.severe("Repairing migrations and disabling plugin..");
            try {
                flyway.repair();
            } catch (FlywayException ex2) {
                WacCore.logger.severe("Error whilst attempting to repair migrations:");
                ex2.printStackTrace();
                WacCore.logger.severe("Continuing to disable plugin..");
            }
            return false;
        }

        return true;
    }
}
