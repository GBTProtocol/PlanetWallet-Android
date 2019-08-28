package io.grabity.planetwallet.MiniFramework.wallet.store;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import io.grabity.planetwallet.MiniFramework.managers.DatabaseManager.PWDBManager;
import io.grabity.planetwallet.MiniFramework.utils.PLog;
import io.grabity.planetwallet.MiniFramework.wallet.cointype.CoinType;
import io.grabity.planetwallet.VO.MainItems.MainItem;
import io.grabity.planetwallet.VO.Planet;

public class PlanetStore {

    private static final PlanetStore instance = new PlanetStore( );
    private final Map< String, Planet > items = new HashMap<>( );

    private PlanetStore( ) {
        if ( PWDBManager.getInstance( ) != null ) {
            ArrayList< Planet > planets = PWDBManager.getInstance( ).select( Planet.class );
            for ( Planet planet : planets ) {
                items.put( planet.getKeyId( ), planet );
            }
        }
    }

    public static PlanetStore getInstance( ) {
        return instance;
    }

    public Planet getPlanet( String keyId ) {
        return items.get( keyId );
    }

    public ArrayList< Planet > getPlanetList( ) {
        return getPlanetList( true );
    }

    public ArrayList< Planet > getPlanetList( String symbol ) {
        return getPlanetList( symbol, true );
    }

    public ArrayList< Planet > getPlanetList( boolean all ) {
        return PWDBManager.getInstance( ).select( Planet.class, "Planet", String.format( "%s", all ? "" : "hide='N'" ), null );
    }

    public ArrayList< Planet > getPlanetList( String symbol, boolean all ) {
        return PWDBManager.getInstance( ).select( Planet.class, "Planet", String.format( "symbol = '%s' %s", symbol, all ? "" : "AND hide='N'" ), null );
    }

    public String save( Planet planet ) {
        String id = planet.getKeyId( );
        if ( !this.items.containsKey( id ) ) {
            PWDBManager.getInstance( ).insertData( planet );

            // Coin Main Item add
            PLog.e( "add Main Item insert func" );
            MainItem mainItem = new MainItem( );
            mainItem.setKeyId( planet.getKeyId( ) );
            mainItem.setCoinType( planet.getCoinType( ) );
            mainItem.setBalance( "0" );
            mainItem.setHide( "N" );
            mainItem.setName( CoinType.of( planet.getCoinType( ) ).getCoinName( ) );
            mainItem.setSymbol( CoinType.of( planet.getCoinType( ) ).name( ) );
            PWDBManager.getInstance( ).insertData( mainItem );

            this.items.put( planet.getKeyId( ), planet );
        }
        return id;
    }

    public String update( Planet planet ) {
        String id = planet.getKeyId( );
        if ( this.items.containsKey( id ) ) {
            PWDBManager.getInstance( ).updateData( planet, "keyId='" + planet.getKeyId( ) + "'" );
            ArrayList< Planet > planets = PWDBManager.getInstance( ).select( Planet.class );
            for ( Planet p : planets ) {
                items.put( p.getKeyId( ), p );
            }
        }
        return id;
    }

    public void delete( String s ) {
        if ( this.items.containsKey( s ) ) {
            Planet planet = new Planet( );
            planet.setKeyId( s );
            PWDBManager.getInstance( ).deleteData( planet );
            this.items.remove( s );
        }
    }
}
