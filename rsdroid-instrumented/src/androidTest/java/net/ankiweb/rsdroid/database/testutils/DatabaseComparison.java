/*
 * Copyright (c) 2020 David Allison <davidallisongithub@gmail.com>
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.ankiweb.rsdroid.database.testutils;

import androidx.annotation.NonNull;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;

import net.ankiweb.rsdroid.Backend;
import net.ankiweb.rsdroid.BackendFactory;
import net.ankiweb.rsdroid.ankiutil.InstrumentedTest;
import net.ankiweb.rsdroid.database.AnkiSupportSQLiteDatabase;

import org.junit.Before;
import org.junit.runners.Parameterized;

import java.util.Arrays;

public class DatabaseComparison extends InstrumentedTest {

    @Parameterized.Parameter
    public DatabaseType schedVersion;
    protected SupportSQLiteDatabase mDatabase;

    @Parameterized.Parameters(name = "{0}")
    public static java.util.Collection<Object[]> initParameters() {
        // This does one run with schedVersion injected as 1, and one run as 2
        return Arrays.asList(new Object[][]{{DatabaseType.FRAMEWORK}, {DatabaseType.RUST_LEGACY}, {DatabaseType.RUST_NEW}});
    }

    @Before
    public void setUp() {
        try {
            mDatabase = getDatabase();
            mDatabase.execSQL("create table nums (id int)");
        } catch (Exception e) {
            if (!handleSetupException(e)) {
                throw e;
            }
        }
    }

    protected boolean handleSetupException(Exception e) {
        return false;
    }

    protected SupportSQLiteDatabase getDatabase() {
        switch (schedVersion) {
            case RUST_LEGACY:
                Backend backend = BackendFactory.getBackend(getContext(), Arrays.asList("en"), true);
                backend.openCollection(getDatabasePath());
                return AnkiSupportSQLiteDatabase.withRustBackend(backend);
            case RUST_NEW:
                Backend backend2 = BackendFactory.getBackend(getContext(), Arrays.asList("en"), false);
                backend2.openCollection(getDatabasePath());
                return AnkiSupportSQLiteDatabase.withRustBackend(backend2);
            case FRAMEWORK:
                return AnkiSupportSQLiteDatabase.withFramework(getContext(), getDatabasePath());
        }
        throw new IllegalStateException();
    }

    protected String getDatabasePath() {
        // TODO: look into this - null should work
        try {
            switch (schedVersion) {
                case RUST_LEGACY:
                case RUST_NEW:
                    return ":memory:";
                case FRAMEWORK:
                    return null;
                default:
                    return null;
            }
        } catch (NullPointerException ex) {
            throw new IllegalStateException("Class is not annotated with @RunWith(Parameterized.class)", ex);
        }
    }

    public enum DatabaseType {
        FRAMEWORK,
        RUST_LEGACY,
        RUST_NEW,
    }


    private static class DefaultCallback extends SupportSQLiteOpenHelper.Callback {
        public DefaultCallback() {
            super(1);
        }

        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {

        }

        @Override
        public void onUpgrade(@NonNull SupportSQLiteDatabase db, int oldVersion, int newVersion) {

        }

        @Override
        public void onCorruption(@NonNull SupportSQLiteDatabase db) {
            // do nothing
        }
    }
}
