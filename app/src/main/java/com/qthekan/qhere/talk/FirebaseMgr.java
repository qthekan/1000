package com.qthekan.qhere.talk;

import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


public class FirebaseMgr {
    private static DatabaseReference mDB;
    String mRoomName;


    public void connect(String room) {
        if (mDB != null) {
            return;
        }

        mRoomName = room;
        mDB = FirebaseDatabase.getInstance().getReference();
        registerUpdateCount();

        //deleteOldDataFromDB();
        //deleteOldDataByCount();
    }


    public void unregievent(String room)
    {
        mDB.child(room).removeEventListener(mEventListener);
        mEventListener = null;
        mDB = null;
    }


    public void insert(String room, String nick, String msg)
    {
        if("".equalsIgnoreCase(msg))
        {
            return;
        }

        Message message = new Message(System.currentTimeMillis(), nick, msg);
        mDB.child(room).push().setValue(message);
        Log.i("insert()", message.toString());

        deleteOldDataByCount();
    }


    ChildEventListener mEventListener = null;
    public void select(String room)
    {
        if(mEventListener == null)
        {
            mEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    if(dataSnapshot.getKey().equalsIgnoreCase("count") )
                    {
                        return;
                    }

                    try
                    {
                        Message msg = dataSnapshot.getValue(Message.class);
                        Log.i("select()", msg.toString());
                        ChatService.ins.appendContents(msg.toString());
                    }
                    catch (Exception e)
                    {
                        Log.e("select()", "error:" + dataSnapshot.getKey() + "\n" + e);
                    }
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };
        }

        mDB.child(room).addChildEventListener(mEventListener);
    }


    public void deleteOldDataFromDB()
    {
        long cutoff = new Date().getTime() - TimeUnit.MILLISECONDS.convert(30, TimeUnit.SECONDS);
        Query oldItems = mDB.orderByChild("timestamp").endAt(cutoff);
        oldItems.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot itemSnapshot: snapshot.getChildren()) {
                    itemSnapshot.getRef().removeValue();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                throw databaseError.toException();
            }
        });
    }


    public void deleteOldDataByCount()
    {
        int remainDataCount = 50;
        int deleteDataCount = mDataCount - remainDataCount;
        Log.i("deleteOldDataByCount()", "deleteDataCount:" + deleteDataCount);
        if(deleteDataCount <= 0)
        {
            return;
        }

        Query oldItems = mDB.child(mRoomName).orderByChild("mTime").limitToFirst(deleteDataCount);
        oldItems.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot itemSnapshot: dataSnapshot.getChildren()) {
                    itemSnapshot.getRef().removeValue();
                    Log.i("deleteOldDataByCount()", "msg:" + itemSnapshot.getValue().toString() );
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                throw databaseError.toException();
            }
        });
    }


    /**
     * 데이터 추가, 삭제 시 count 필드를 업데이트 하는 리스너 등록.
     */
    int mDataCount = 0;
    public void registerUpdateCount()
    {
        mDB.child(mRoomName).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if(dataSnapshot.getKey().equalsIgnoreCase("count") )
                {
                    return;
                }

                mDB.child(mRoomName).child("count").setValue(++mDataCount);
                Log.i("registerUpdateCount()", "count++:" + mDataCount);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                mDB.child(mRoomName).child("count").setValue(--mDataCount);
                Log.i("registerUpdateCount()", "count--:" + mDataCount);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}
