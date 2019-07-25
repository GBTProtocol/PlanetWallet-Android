package io.grabity.planetwallet.Widgets.OverScrollWrapper;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.DecelerateInterpolator;
import android.widget.RelativeLayout;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import io.grabity.planetwallet.MiniFramework.utils.Utils;
import io.grabity.planetwallet.Widgets.AdvanceRecyclerView.AdvanceRecyclerView;


/**
 * Created by JcobPark on 15. 8. 13..
 */
public class OverScrollWrapper extends RelativeLayout {

    private RecyclerView listView;

    public OverScrollWrapper( Context context ) {
        super( context );
        touchSlop = ViewConfiguration.get( context ).getScaledTouchSlop( );
        setWillNotDraw( false );
        setOnHierarchyChangeListener( onHierarchyChangeListener );
        onRefreshListeners = new ArrayList<>( );
    }

    public OverScrollWrapper( Context context, AttributeSet attrs ) {
        super( context, attrs );
        touchSlop = ViewConfiguration.get( context ).getScaledTouchSlop( );
        setWillNotDraw( false );
        setOnHierarchyChangeListener( onHierarchyChangeListener );
        onRefreshListeners = new ArrayList<>( );
    }

    OnHierarchyChangeListener onHierarchyChangeListener = new OnHierarchyChangeListener( ) {
        @Override
        public void onChildViewAdded( View parent, View child ) {
            if ( child instanceof RecyclerView ) {
                listView = ( RecyclerView ) child;
            }
        }

        @Override
        public void onChildViewRemoved( View parent, View child ) {

        }
    };

    private static final int INVALID_POINTER = 48 * 4;
    private static final float DRAG_RATE = .5f;

    private int touchSlop;

    private float initialMotionY;
    private float initialDownY;
    private boolean isDragged;
    private int activePointerId = INVALID_POINTER;

    private boolean returningToStart;

    private float singleTouchLastY;
    private float multiTouchLastY;

    private float multiTouchStartY = 0f;
    private float multiTouchMoveY = 0f;
    private float multiTouchSubtract = 0f;

    private float againMultiTouchStartY = 0f;
    private float againMultiTouchMoveY = 0f;
    private float againMultiTouchSubtract = 0f;

    private boolean multiTouch = false;
    private boolean multiTouchAgain = false;

    private boolean isRefreshing = false;
    private boolean viberate = false;

    private ArrayList< OnRefreshListener > onRefreshListeners;


    public boolean canChildScrollUp( ) {
        if ( this.listView != null )
            return this.listView.canScrollVertically( -1 );
        else return false;
    }


    @Override
    public boolean onInterceptTouchEvent( MotionEvent ev ) {
        if ( isRefreshing ) return true;
        viberate = false;

        final int action = ev.getActionMasked( );

        if ( returningToStart && action == MotionEvent.ACTION_DOWN ) {
            returningToStart = false;
        }

        if ( !isEnabled( ) || returningToStart || canChildScrollUp( ) ) {
            return false;
        }


        switch ( action ) {
            case MotionEvent.ACTION_DOWN:
                activePointerId = ev.getPointerId( 0 );
                isDragged = false;
                final float initialDownY = getMotionEventY( ev, activePointerId );
                if ( initialDownY == -1 ) {
                    return false;
                }
                this.initialDownY = initialDownY;

                break;

            case MotionEvent.ACTION_MOVE:
                if ( activePointerId == INVALID_POINTER ) {
                    return false;
                }

                final float y = getMotionEventY( ev, activePointerId );
                if ( y == -1 ) {
                    return false;
                }
                final float yDiff = y - this.initialDownY;

                if ( yDiff > touchSlop && !isDragged ) {
                    initialMotionY = this.initialDownY + touchSlop;
                    isDragged = true;
                }
                break;

            case MotionEvent.ACTION_POINTER_UP:
                onSecondaryPointerUp( ev );
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                isDragged = false;
                activePointerId = INVALID_POINTER;
                break;
        }

        return isDragged;
    }


    private float getMotionEventY( MotionEvent ev, int activePointerId ) {
        final int index = ev.findPointerIndex( activePointerId );
        if ( index < 0 ) {
            return -1;
        }
        return ev.getY( index );
    }


    @Override
    public boolean onTouchEvent( MotionEvent ev ) {
        if ( isRefreshing ) return false;

        final int action = ev.getActionMasked( );
        if ( returningToStart && action == MotionEvent.ACTION_DOWN ) {
            returningToStart = false;
        }

        if ( !isEnabled( ) || returningToStart || canChildScrollUp( ) ) {
            return false;
        }

        switch ( action ) {
            case MotionEvent.ACTION_DOWN:
                activePointerId = ev.getPointerId( 0 );
                isDragged = false;
                break;

            case MotionEvent.ACTION_MOVE: {
                final int pointerIndex = 0;
                ev.findPointerIndex( activePointerId );
                if ( pointerIndex < 0 ) {
                    return false;
                }

                final float y = ev.getY( pointerIndex );
                if ( !multiTouch ) {
                    singleTouchLastY = listView.getTop( );
                } else {
                    if ( !multiTouchAgain ) {
                        if ( multiTouchStartY == 0 ) {
                            multiTouchStartY = y;
                        } else {
                            multiTouchMoveY = y;
                            multiTouchSubtract = multiTouchMoveY - multiTouchStartY;
                            multiTouchLastY = listView.getTop( );
                        }
                    } else {
                        if ( againMultiTouchStartY == 0 ) {
                            againMultiTouchStartY = y;
                        } else {
                            againMultiTouchMoveY = y;
                            againMultiTouchSubtract = againMultiTouchMoveY - againMultiTouchStartY;
                        }

                    }
                }
                float overscrollTop = 0;

                if ( multiTouch ) {
                    if ( !multiTouchAgain ) {
                        overscrollTop = singleTouchLastY + multiTouchSubtract;
                    } else {
                        overscrollTop = multiTouchLastY + againMultiTouchSubtract;
                    }
                } else {
                    overscrollTop = ( y - initialMotionY ) * DRAG_RATE;
                }

                if ( isDragged ) {
                    if ( this.getChildAt( 0 ) != null ) {
                        if ( this.getChildAt( 0 ).getTop( ) >= 0 ) {
                            this.getChildAt( 0 ).setTop( ( int ) overscrollTop );
                            if ( listView instanceof AdvanceRecyclerView ) {
                                if ( ( ( AdvanceRecyclerView ) listView ).getOnScrollListeners( ) != null ) {
                                    for ( AdvanceRecyclerView.OnScrollListener listener : ( ( AdvanceRecyclerView ) listView ).getOnScrollListeners( ) ) {
                                        listener.onScrolled( listView, 0, 0, 0, -overscrollTop );
                                    }
                                }
                            }
                        }
                    }
                }

                if ( Utils.dpToPx( getContext( ), 120 ) < overscrollTop ) {
                    if ( !viberate ) {
                        Utils.vibrate( getContext( ), 25 );
                        viberate = true;
                    }
                }

                break;
            }
            case MotionEvent.ACTION_POINTER_DOWN: {
                final int index = ev.getActionIndex( );
                activePointerId = ev.getPointerId( index );

                if ( multiTouchAgain ) {
                    againMultiTouchStartY = 0;
                    againMultiTouchMoveY = 0;
                    againMultiTouchSubtract = 0;
                    multiTouchLastY = listView.getTop( );

                }
                if ( multiTouch ) {
                    multiTouchAgain = true;
                }

                break;
            }

            case MotionEvent.ACTION_POINTER_UP:
                onSecondaryPointerUp( ev );

                if ( multiTouchAgain ) {
                    againMultiTouchStartY = 0;
                    againMultiTouchMoveY = 0;
                    againMultiTouchSubtract = 0;
                    multiTouchLastY = listView.getTop( );
                }

                multiTouch = true;


                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {

                if ( Utils.dpToPx( getContext( ), 120 ) < this.getChildAt( 0 ).getTop( ) ) {
                    isRefreshing = true;
                }


                singleTouchLastY = 0;
                multiTouchLastY = 0;
                multiTouchStartY = 0;
                multiTouchMoveY = 0;
                multiTouchSubtract = 0;
                againMultiTouchStartY = 0;
                againMultiTouchMoveY = 0;
                againMultiTouchSubtract = 0;

                multiTouch = false;
                multiTouchAgain = false;

                if ( activePointerId == INVALID_POINTER ) {
                    if ( action == MotionEvent.ACTION_UP ) {
                    }
                    return false;
                }
                isDragged = false;
                startRestorePosition( this.getChildAt( 0 ).getTop( ) );
                activePointerId = INVALID_POINTER;


                return false;
            }
        }

        return true;
    }


    private void onSecondaryPointerUp( MotionEvent ev ) {
        final int pointerIndex = ev.getActionIndex( );
        final int pointerId = ev.getPointerId( pointerIndex );

        if ( pointerId == activePointerId ) {
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            activePointerId = ev.getPointerId( newPointerIndex );
        }
    }


    private void startRestorePosition( float yPosition ) {

        if ( isRefreshing ) {
            if ( onRefreshListeners != null ) {
                for ( int i = 0; i < onRefreshListeners.size( ); i++ ) {
                    onRefreshListeners.get( i ).onRefresh( );
                }

            }
        }

        ObjectAnimator animator = ObjectAnimator.ofInt( this.getChildAt( 0 ), "top", ( int ) yPosition, isRefreshing ? ( int ) Utils.dpToPx( getContext( ), 80 ) : 0 );
        animator.setDuration( 300 );
        animator.setStartDelay( 0 );
        animator.addUpdateListener( new ValueAnimator.AnimatorUpdateListener( ) {
            @Override
            public void onAnimationUpdate( ValueAnimator animation ) {
                if ( listView instanceof AdvanceRecyclerView ) {
                    if ( ( ( AdvanceRecyclerView ) listView ).getOnScrollListeners( ) != null ) {
                        for ( AdvanceRecyclerView.OnScrollListener listener : ( ( AdvanceRecyclerView ) listView ).getOnScrollListeners( ) ) {
                            listener.onScrolled( listView, 0, 0, 0, -( Integer ) animation.getAnimatedValue( ) );
                        }
                    }
                }
            }
        } );
        animator.setInterpolator( new DecelerateInterpolator( 2f ) );
        animator.addListener( new Animator.AnimatorListener( ) {
            @Override
            public void onAnimationStart( Animator animation ) {
                returningToStart = true;
            }

            @Override
            public void onAnimationEnd( Animator animation ) {
                returningToStart = false;
            }

            @Override
            public void onAnimationCancel( Animator animation ) {
                returningToStart = false;
            }

            @Override
            public void onAnimationRepeat( Animator animation ) {

            }
        } );
        animator.start( );

    }

    public boolean isRefreshing( ) {
        return isRefreshing;
    }

    public void completeRefresh( ) {
        this.isRefreshing = false;
        startRestorePosition( Utils.dpToPx( getContext( ), 80 ) );
    }

    public OnRefreshListener getOnRefreshListener( int index ) {
        if ( onRefreshListeners != null )
            return onRefreshListeners.get( index );
        else
            return null;
    }

    public void addOnRefreshListener( OnRefreshListener onRefreshListener ) {
        if ( onRefreshListeners == null ) onRefreshListeners = new ArrayList<>( );
        if ( onRefreshListener != null )
            this.onRefreshListeners.add( onRefreshListener );
    }

    public interface OnRefreshListener {
        void onRefresh( );
    }
}