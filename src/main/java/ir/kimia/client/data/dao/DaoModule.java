package ir.kimia.client.data.dao;

import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import dagger.Module;
import dagger.Provides;
import ir.kimia.client.data.dao.api.*;
import ir.kimia.client.data.dao.impl.*;
import ir.kimia.client.data.model.*;
import ir.kimia.client.util.FxUtil;
import ir.kimia.client.util.PropUtil;

import java.sql.SQLException;

/**
 * Class for providing Dagger beans for DAO services.
 *
 * @author Amir
 */
@Module
public class DaoModule {

    private final static String DATABASE_URL = PropUtil.getString("h2.connection.string");

    private ConnectionSource getConnectionSource() throws SQLException {
        // TODO: check if static connectionSource works better.
        // TODO: read user/pass from properties.
        JdbcConnectionSource connectionSource = new JdbcConnectionSource(DATABASE_URL, "sa", "");

        TableUtils.createTableIfNotExists(connectionSource, Product.class);
        TableUtils.createTableIfNotExists(connectionSource, Address.class);
        TableUtils.createTableIfNotExists(connectionSource, ProductCategory.class);
        TableUtils.createTableIfNotExists(connectionSource, Office.class);
        TableUtils.createTableIfNotExists(connectionSource, User.class);
        TableUtils.createTableIfNotExists(connectionSource, Party.class);
        TableUtils.createTableIfNotExists(connectionSource, Stock.class);
        TableUtils.createTableIfNotExists(connectionSource, Bank.class);
        TableUtils.createTableIfNotExists(connectionSource, Cheque.class);
        TableUtils.createTableIfNotExists(connectionSource, SystemParameter.class);
        TableUtils.createTableIfNotExists(connectionSource, Stone.class);
        TableUtils.createTableIfNotExists(connectionSource, StoneType.class);
        TableUtils.createTableIfNotExists(connectionSource, PurityEvaluator.class);
        TableUtils.createTableIfNotExists(connectionSource, Fineness.class);
        TableUtils.createTableIfNotExists(connectionSource, Invoice.class);
        TableUtils.createTableIfNotExists(connectionSource, InvoiceRecord.class);
        TableUtils.createTableIfNotExists(connectionSource, AccountBalance.class);
        TableUtils.createTableIfNotExists(connectionSource, Report.class);

        return connectionSource;
    }

    @Provides
    public ReportDao reportDao() {
        try {
            return new ReportDaoImpl(getConnectionSource(), Report.class);
        } catch (SQLException sqlException) {
            FxUtil.exceptionOccurred(sqlException);
        }
        return null;
    }

    @Provides
    public UserDao userDao() {
        try {
            return new UserDaoImpl(getConnectionSource(), User.class);
        } catch (SQLException sqlException) {
            FxUtil.exceptionOccurred(sqlException);
        }
        return null;
    }

    @Provides
    public OfficeDao officeDao() {
        try {
            return new OfficeDaoImpl(getConnectionSource(), Office.class);
        } catch (SQLException sqlException) {
            FxUtil.exceptionOccurred(sqlException);
        }
        return null;
    }

    @Provides
    public ProductCategoryDao productCategoryDao() {
        try {
            return new ProductCategoryDaoImpl(getConnectionSource(), ProductCategory.class);
        } catch (SQLException sqlException) {
            FxUtil.exceptionOccurred(sqlException);
        }
        return null;
    }

    @Provides
    public ProductDao productDao() {
        try {
            return new ProductDaoImpl(getConnectionSource(), Product.class);
        } catch (SQLException sqlException) {
            FxUtil.exceptionOccurred(sqlException);
        }
        return null;
    }

    @Provides
    public PartyDao partyDao() {
        try {
            return new PartyDaoImpl(getConnectionSource(), Party.class);
        } catch (SQLException sqlException) {
            FxUtil.exceptionOccurred(sqlException);
        }
        return null;
    }

    @Provides
    public StockDao stockDao() {
        try {
            return new StockDaoImpl(getConnectionSource(), Stock.class);
        } catch (SQLException sqlException) {
            FxUtil.exceptionOccurred(sqlException);
        }
        return null;
    }

    @Provides
    public AddressDao addressDao() {
        try {
            return new AddressDaoImpl(getConnectionSource(), Address.class);
        } catch (SQLException sqlException) {
            FxUtil.exceptionOccurred(sqlException);
        }
        return null;
    }

    @Provides
    public BankDao bankDao() {
        try {
            return new BankDaoImpl(getConnectionSource(), Bank.class);
        } catch (SQLException sqlException) {
            FxUtil.exceptionOccurred(sqlException);
        }
        return null;
    }

    @Provides
    public ChequeDao chequeDao() {
        try {
            return new ChequeDaoImpl(getConnectionSource(), Cheque.class);
        } catch (SQLException sqlException) {
            FxUtil.exceptionOccurred(sqlException);
        }
        return null;
    }

    @Provides
    public SystemParameterDao systemParameterDao() {
        try {
            return new SystemParameterDaoImpl(getConnectionSource(), SystemParameter.class);
        } catch (SQLException sqlException) {
            FxUtil.exceptionOccurred(sqlException);
        }
        return null;
    }

    @Provides
    public StoneDao stoneDao() {
        try {
            return new StoneDaoImpl(getConnectionSource(), Stone.class);
        } catch (SQLException sqlException) {
            FxUtil.exceptionOccurred(sqlException);
        }
        return null;
    }

    @Provides
    public StoneTypeDao stoneTypeDao() {
        try {
            return new StoneTypeDaoImpl(getConnectionSource(), StoneType.class);
        } catch (SQLException sqlException) {
            FxUtil.exceptionOccurred(sqlException);
        }
        return null;
    }

    @Provides
    public PurityEvaluatorDao purityEvaluatorDao() {
        try {
            return new PurityEvaluatorDaoImpl(getConnectionSource(), PurityEvaluator.class);
        } catch (SQLException sqlException) {
            FxUtil.exceptionOccurred(sqlException);
        }
        return null;
    }

    @Provides
    public FinenessDao finenessDao() {
        try {
            return new FinenessDaoImpl(getConnectionSource(), Fineness.class);
        } catch (SQLException sqlException) {
            FxUtil.exceptionOccurred(sqlException);
        }
        return null;
    }

    @Provides
    public InvoiceDao invoiceDao() {
        try {
            return new InvoiceDaoImpl(getConnectionSource(), Invoice.class);
        } catch (SQLException sqlException) {
            FxUtil.exceptionOccurred(sqlException);
        }
        return null;
    }

    @Provides
    public InvoiceRecordDao invoiceRecordDao() {
        try {
            return new InvoiceRecordDaoImpl(getConnectionSource(), InvoiceRecord.class);
        } catch (SQLException sqlException) {
            FxUtil.exceptionOccurred(sqlException);
        }
        return null;
    }

    @Provides
    public AccountBalanceDao accountBalanceDao() {
        try {
            return new AccountBalanceDaoImpl(getConnectionSource(), AccountBalance.class);
        } catch (SQLException sqlException) {
            FxUtil.exceptionOccurred(sqlException);
        }
        return null;
    }

}
