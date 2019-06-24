package io.grabity.planetwallet.Views.p4_Main.Activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;

import java.util.ArrayList;

import io.grabity.planetwallet.Common.commonset.C;
import io.grabity.planetwallet.Common.components.PlanetWalletActivity;
import io.grabity.planetwallet.MiniFramework.utils.Utils;
import io.grabity.planetwallet.MiniFramework.wallet.cointype.CoinType;
import io.grabity.planetwallet.MiniFramework.wallet.store.ERC20Store;
import io.grabity.planetwallet.MiniFramework.wallet.store.PlanetStore;
import io.grabity.planetwallet.R;
import io.grabity.planetwallet.VO.MainItems.ERC20;
import io.grabity.planetwallet.VO.MainItems.ETH;
import io.grabity.planetwallet.VO.Planet;
import io.grabity.planetwallet.Views.p4_Main.Adapter.MainAdapter;
import io.grabity.planetwallet.Views.p4_Main.Adapter.PlanetAdapter;
import io.grabity.planetwallet.Views.p4_Main.Etc.ViewController;
import io.grabity.planetwallet.Views.p4_Main.Popups.ERC20Popup;
import io.grabity.planetwallet.Views.p5_Token.Activity.TokenAddActivity;
import io.grabity.planetwallet.Views.p6_Transfer.Activity.TransferActivity;
import io.grabity.planetwallet.Views.p7_Setting.Activity.SettingActivity;
import io.grabity.planetwallet.Widgets.AdvanceRecyclerView.AdvanceArrayAdapter;
import io.grabity.planetwallet.Widgets.AdvanceRecyclerView.AdvanceRecyclerView;
import io.grabity.planetwallet.Widgets.BarcodeView;
import io.grabity.planetwallet.Widgets.CustomToast;
import io.grabity.planetwallet.Widgets.OverScrollWrapper.OverScrollWrapper;
import io.grabity.planetwallet.Widgets.PlanetView;
import io.grabity.planetwallet.Widgets.RippleEffectView;
import io.grabity.planetwallet.Widgets.RoundButton.RoundButton;
import io.grabity.planetwallet.Widgets.ShadowView;
import io.grabity.planetwallet.Widgets.SlideDrawerLayout;
import io.grabity.planetwallet.Widgets.StretchImageView;
import io.grabity.planetwallet.Widgets.ToolBar;

public class MainActivity extends PlanetWalletActivity implements AdvanceArrayAdapter.OnAttachViewListener, ToolBar.OnToolBarClickListener, RippleEffectView.OnRippleEffectListener, AdvanceRecyclerView.OnItemClickListener, OverScrollWrapper.OnRefreshListener {

    private ViewMapper viewMapper;
    private HeaderViewMapper headerViewMapper;
    private FooterViewMapper footerViewMapper;
    private ViewController viewController;

    private Planet selectedPlanet;

    private ArrayList< Planet > planetList;

    @Override
    protected void onCreate( @Nullable Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setStatusTransparent( true );
        setContentView( R.layout.activity_main );
        viewMapper = new ViewMapper( );
        viewInit( );
        setData( );
    }

    @Override
    protected void viewInit( ) {
        super.viewInit( );
        ( ( ViewGroup ) findViewById( android.R.id.content ) ).addView( viewMapper.rippleView );
        ( ( ViewGroup.MarginLayoutParams ) viewMapper.toolBar.getLayoutParams( ) ).height = ( int ) ( Utils.dpToPx( this, 68 ) + getResources( ).getDimensionPixelSize( getResources( ).getIdentifier( "status_bar_height", "dimen", "android" ) ) );
        viewMapper.toolBar.requestLayout( );

        viewMapper.overScrollWrapper.addOnRefreshListener( this );


        viewMapper.toolBar.setLeftButton( new ToolBar.ButtonItem( ).setTag( C.tag.TOOLBAR_MENU ) );
        viewMapper.toolBar.setRightButton( new ToolBar.ButtonItem( ).setTag( C.tag.TOOLBAR_MUTIUNIVERSE ) );
        viewMapper.toolBar.setOnToolBarClickListener( this );

        viewMapper.rippleView.setOnRippleEffectListener( this );
        viewMapper.rippleView.setTrigger( viewMapper.toolBar.getButtonItems( ).get( 0 ).getView( ) );

        viewMapper.listMain.setOnItemClickListener( this );
        viewMapper.listMain.setOnAttachViewListener( this );
        viewMapper.listMain.addHeaderView( R.layout.header_main );
        viewMapper.listMain.addFooterView( R.layout.footer_main );

        viewMapper.listPlanets.setOnItemClickListener( this );

        viewMapper.slideDrawer.setTrigger( SlideDrawerLayout.Position.TOP, viewMapper.toolBar.getButtonItems( ).get( 1 ).getView( ) );

        viewMapper.btnCopy.setOnClickListener( this );
        viewMapper.btnTransfer.setOnClickListener( this );
        viewMapper.btnBottomBlur.setOnClickListener( this );

        viewMapper.slideDrawer.addBypassArea( viewMapper.btnBottomBlur );

        viewController = new ViewController( this, viewMapper );
    }

    @Override
    protected void setData( ) {
        super.setData( );
        planetList = PlanetStore.getInstance( ).getPlanetList( false );
        viewMapper.listPlanets.setAdapter( new PlanetAdapter( this, planetList ) );

        String keyId = String.valueOf( Utils.getPreferenceData( this, C.pref.LAST_PLANET_KEYID, "" ) );
        if ( keyId.length( ) == 0 ) {
            if ( planetList.size( ) > 0 ) {
                selectedPlanet = planetList.get( 0 );
                setUpViews( );
            }
        } else {

            if ( planetList.size( ) == 1 ) {
                selectedPlanet = planetList.get( 0 );
                setUpViews( );
            } else {
                for ( Planet planet : planetList ) {
                    if ( Utils.equals( planet.getKeyId( ), keyId ) ) {
                        selectedPlanet = planet;
                        setUpViews( );
                        break;
                    }
                }
            }
        }

    }

    void setUpViews( ) {

        if ( selectedPlanet != null ) {
            Utils.setPreferenceData( this, C.pref.LAST_PLANET_KEYID, selectedPlanet.getKeyId( ) );

            if ( Utils.equals( CoinType.ETH.getCoinType( ), selectedPlanet.getCoinType( ) ) ) {
                ArrayList< ERC20 > tokenList = ERC20Store.getInstance( ).getTokenList( selectedPlanet.getKeyId( ), false );
                selectedPlanet.setItems( new ArrayList<>( ) );
                ETH eth = new ETH( );
                eth.setAddress( selectedPlanet.getAddress( ) );
                selectedPlanet.getItems( ).add( eth );
                for ( ERC20 erc20 : tokenList ) {
                    selectedPlanet.getItems( ).add( erc20 );
                }
            }
            viewMapper.listMain.setAdapter( new MainAdapter( this, selectedPlanet.getItems( ) == null ? new ArrayList<>( ) : selectedPlanet.getItems( ) ) );

            viewMapper.textPlanetName.setText( selectedPlanet.getName( ) );
            viewMapper.toolBar.setTitle( CoinType.of( selectedPlanet.getCoinType( ) ).name( ) );
            viewMapper.barcodeView.setData( selectedPlanet.getAddress( ) );
            viewMapper.textBottomPlanetName.setText( selectedPlanet.getName( ) );
            viewMapper.textBottomAddress.setText( selectedPlanet.getAddress( ) );

            viewMapper.planetBackground.setData( selectedPlanet.getAddress( ) );
            viewMapper.planetBlur.setData( selectedPlanet.getAddress( ) );

            if ( viewController != null )
                viewController.updateBlurView( getCurrentTheme( ) );
        }

    }

    @Override
    public void onAttachView( int resId, int position, View view, boolean screenShotFlag ) {

        if ( !screenShotFlag ) {
            if ( resId == R.layout.header_main && position == 0 ) {

                headerViewMapper = new HeaderViewMapper( view );
                headerViewMapper.groupAddress.setOnClickListener( this );
                headerViewMapper.planetView.setData( selectedPlanet.getAddress( ) );
                headerViewMapper.textAddress.setText( Utils.addressReduction( selectedPlanet.getAddress( ) ) );
                headerViewMapper.textName.setText( selectedPlanet.getName( ) );
                viewMapper.barcodeView.setPlanetView( headerViewMapper.planetView );

                if ( viewController != null )
                    viewController.setHeaderViewMapper( headerViewMapper );


            } else if ( resId == R.layout.footer_main ) {

                footerViewMapper = new FooterViewMapper( view );
                footerViewMapper.btnAddToken.setOnClickListener( this );
                footerViewMapper.groupAddToken.setVisibility( selectedPlanet.getCoinType( ).equals( CoinType.ETH.getCoinType( ) ) ? View.VISIBLE : View.GONE );

                if ( selectedPlanet.getCoinType( ).equals( CoinType.BTC.getCoinType( ) ) ) {
                    if ( selectedPlanet.getItems( ) != null )
                        footerViewMapper.groupMessage.setVisibility( selectedPlanet.getItems( ).size( ) == 0 ? View.VISIBLE : View.GONE );
                    else
                        footerViewMapper.groupMessage.setVisibility( View.VISIBLE );
                }

                footerViewMapper.btnAddToken.setBorderColorNormal( Color.parseColor( getCurrentTheme( ) ? "#EDEDED" : "#1E1E28" ) );
                footerViewMapper.btnAddToken.setBorderColorHighlight( Color.parseColor( getCurrentTheme( ) ? "#EDEDED" : "#1E1E28" ) );

            }
        }

    }

    @Override
    public void onClick( View v ) {
        super.onClick( v );
        if ( v == viewMapper.btnCopy ) {

            Utils.copyToClipboard( this, selectedPlanet.getAddress( ) );
            CustomToast.makeText( this, localized( R.string.main_btn_bottom_copy_title ) ).show( );

        } else if ( v == headerViewMapper.groupAddress ) {

            Utils.copyToClipboard( this, selectedPlanet.getAddress( ) );
            CustomToast.makeText( this, localized( R.string.main_btn_bottom_copy_title ) ).show( );

        } else if ( v == viewMapper.btnTransfer ) {

            viewMapper.slideDrawer.close( );

            new Handler( ).postDelayed( ( ) -> {
                setTransition( Transition.SLIDE_SIDE );
                sendAction( TransferActivity.class,
                        Utils.createSerializableBundle( selectedPlanet.getName( ).equals( C.bundleKey.BTC ) ? C.bundleKey.BTC : C.bundleKey.ETH, selectedPlanet ) );
            }, 250 );


        } else if ( v == footerViewMapper.btnAddToken ) {

            setTransition( Transition.SLIDE_SIDE );
            sendAction( C.requestCode.MAIN_TOKEN_ADD, TokenAddActivity.class, Utils.createSerializableBundle( C.bundleKey.PLANET, selectedPlanet ) );

        } else if ( v == viewMapper.btnBottomBlur ) {

            viewMapper.slideDrawer.open( SlideDrawerLayout.Position.BOTTOM );

        }
    }

    @Override
    protected void onActivityResult( int requestCode, int resultCode, @Nullable Intent data ) {
        super.onActivityResult( requestCode, resultCode, data );
        if ( requestCode == C.requestCode.MAIN_TOKEN_ADD && resultCode == RESULT_OK ) {
            setUpViews( );
        }
    }

    @Override
    public void onToolBarClick( Object tag, View view ) {
        if ( Utils.equals( tag, C.tag.TOOLBAR_MENU ) ) {
            if ( !viewMapper.rippleView.isRippleOn( ) )
                viewMapper.rippleView.ripple( true );
        } else if ( Utils.equals( tag, C.tag.TOOLBAR_MUTIUNIVERSE ) ) {
            viewMapper.slideDrawer.open( SlideDrawerLayout.Position.TOP );
        }
    }

    @Override
    public void onBackPressed( ) {
        if ( viewMapper.slideDrawer.isOpen( ) ) {
            viewMapper.slideDrawer.close( );
        } else {
            setTransition( Transition.NO_ANIMATION );
            super.onBackPressed( );
        }
    }

    @Override
    protected void onResume( ) {
        super.onResume( );
        viewMapper.rippleView.ripple( false );
        viewMapper.rippleView.setTheme( getCurrentTheme( ) );
        viewMapper.shadowBackground.setShadowColor(
                Color.parseColor( getCurrentTheme( ) ? "#FFFFFF" : "#000000" ),
                Color.parseColor( getCurrentTheme( ) ? "#C8FFFFFF" : "#A8000000" )
        );

        planetList = PlanetStore.getInstance( ).getPlanetList( false );
        viewMapper.listPlanets.setAdapter( new PlanetAdapter( this, planetList ) );
        if ( headerViewMapper != null && headerViewMapper.textName != null ) {
            String planetName = PlanetStore.getInstance( ).getPlanet( selectedPlanet.getKeyId( ) ).getName( );
            selectedPlanet.setName( planetName );
            headerViewMapper.textName.setText( planetName );
        }

    }

    @Override
    public void onRippleEffect( boolean on ) {
        if ( on ) {
            setTransition( Transition.NO_ANIMATION );
            sendAction( SettingActivity.class, Utils.createSerializableBundle( C.bundleKey.PLANET, selectedPlanet ) );
        }
    }

    @Override
    public void onItemClick( AdvanceRecyclerView recyclerView, View view, int position ) {

        if ( recyclerView == viewMapper.listMain ) {

            if ( Utils.equals( selectedPlanet.getItems( ).get( position ).getCoinType( ), CoinType.ETH.getCoinType( ) ) ) {

                viewMapper.slideDrawer.open( SlideDrawerLayout.Position.BOTTOM );

            } else if ( Utils.equals( selectedPlanet.getItems( ).get( position ).getCoinType( ), CoinType.ERC20.getCoinType( ) ) ) {

                ERC20Popup.newInstance( this )
                        .setPlanet( selectedPlanet )
                        .setErc20( ( ERC20 ) selectedPlanet.getItems( ).get( position ) )
                        .setOnERC20PopupClickListener( ( planet, erc20, button ) -> {
                            if ( button == ERC20Popup.COPY ) {

                                Utils.copyToClipboard( MainActivity.this, planet.getAddress( ) );
                                CustomToast.makeText( MainActivity.this, localized( R.string.main_btn_bottom_copy_title ) ).show( );

                            } else if ( button == ERC20Popup.TRANSFER ) {

                                sendAction( TransferActivity.class );

                            }
                        } ).show( );

            }

        } else if ( recyclerView == viewMapper.listPlanets ) {

            viewMapper.slideDrawer.close( );
            selectedPlanet = planetList.get( position );
            setUpViews( );
            viewController.updateBlurView( getCurrentTheme( ) );

        }
    }


    @Override
    protected void onUpdateTheme( boolean theme ) {
        super.onUpdateTheme( theme );
        viewMapper.shadowBackground.setShadowColor(
                Color.parseColor( theme ? "#FFFFFF" : "#000000" ),
                Color.parseColor( theme ? "#C8FFFFFF" : "#AA000000" )
        );
        viewController.updateBlurView( theme );
    }


    @Override
    public void onRefresh( ) {
        new Handler( ).postDelayed( ( ) -> viewMapper.overScrollWrapper.completeRefresh( ), 2500 );
    }


    public class ViewMapper {

        public OverScrollWrapper overScrollWrapper;

        public ToolBar toolBar;
        RippleEffectView rippleView;

        public SlideDrawerLayout slideDrawer;

        public AdvanceRecyclerView listMain;

        public TextView textNotice;
        public View viewTrigger;
        public View groupBlur;
        public StretchImageView imageBlurView;
        public View groupBottom;

        AdvanceRecyclerView listPlanets;
        TextView textPlanetName;

        View btnCopy;
        View btnTransfer;
        BarcodeView barcodeView;


        TextView textBottomPlanetName;
        TextView textBottomAddress;


        public View groupBackground;
        public PlanetView planetBackground;
        public ShadowView shadowBackground;

        PlanetView planetBlur;

        View btnBottomBlur;

        public LottieAnimationView lottiePullToRefresh;

        public ViewMapper( ) {

            overScrollWrapper = findViewById( R.id.refresh );

            toolBar = findViewById( R.id.toolBar );
            rippleView = new RippleEffectView( MainActivity.this );

            slideDrawer = findViewById( R.id.slideDrawer );

            listMain = findViewById( R.id.list_main );

            textNotice = findViewById( R.id.text_main_notice );
            viewTrigger = findViewById( R.id.view_main_bottom_trigger );
            groupBlur = findViewById( R.id.group_main_blur );
            imageBlurView = findViewById( R.id.image_main_blur );
            groupBottom = findViewById( R.id.group_main_bottom );

            listPlanets = findViewById( R.id.list_main_planets_list );
            textPlanetName = findViewById( R.id.text_main_planets_name );

            btnCopy = findViewById( R.id.btn_main_bottom_copy );
            btnTransfer = findViewById( R.id.btn_main_bottom_transfer );
            barcodeView = findViewById( R.id.barcode_main_bottom_barcodeview );

            planetBackground = findViewById( R.id.planet_main_background );
            groupBackground = findViewById( R.id.group_main_background );
            shadowBackground = findViewById( R.id.shadow_main_background );

            planetBlur = findViewById( R.id.planet_main_blur_planetview );

            btnBottomBlur = findViewById( R.id.btn_main_blur );

            lottiePullToRefresh = findViewById( R.id.lottie_main_pull_to_refresh );

            textBottomPlanetName = findViewById( R.id.text_main_bottom_name );
            textBottomAddress = findViewById( R.id.text_main_bottom_address );

        }
    }

    public class HeaderViewMapper {

        public View groupHeaderPlanet;
        View headerView;
        PlanetView planetView;

        TextView textName;
        TextView textAddress;
        View btnCopy;

        ViewGroup groupAddress;

        HeaderViewMapper( View headerView ) {
            this.headerView = headerView;
            groupHeaderPlanet = headerView.findViewById( R.id.group_main_header_planet );
            groupAddress = headerView.findViewById( R.id.group_main_header_address );
            planetView = headerView.findViewById( R.id.planet_main_header );
            textName = headerView.findViewById( R.id.text_main_header_planet_name );
            textAddress = headerView.findViewById( R.id.text_main_header_planet_address );
            btnCopy = headerView.findViewById( R.id.btn_main_header_copy );
        }
    }


    public class FooterViewMapper {

        View footerView;
        RoundButton btnAddToken;

        ViewGroup groupAddToken;
        ViewGroup groupMessage;

        FooterViewMapper( View footerView ) {
            this.footerView = footerView;
            btnAddToken = footerView.findViewById( R.id.btn_footer_main_manage_token );

            groupAddToken = footerView.findViewById( R.id.group_footer_main_manage_token );
            groupMessage = footerView.findViewById( R.id.group_footer_main_bit_message );
        }
    }


}
