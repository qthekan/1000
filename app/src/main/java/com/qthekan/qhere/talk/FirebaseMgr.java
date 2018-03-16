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


public class FirebaseMgr {
    private static DatabaseReference mDB;


    public void connect() {
        if (mDB != null) {
            return;
        }

        mDB = FirebaseDatabase.getInstance().getReference();
        deleteOldDataFromDB();
    }


    public void unregievent(String room)
    {
        mDB.child(room).removeEventListener(mEventListener);
        mEventListener = null;
        mDB = null;
    }


    public void insert(String room, String nick, String msg)
    {
        Message message = new Message(System.currentTimeMillis(), nick, msg);
        mDB.child(room).push().setValue(message);
        Log.i("insert()", message.toString());
    }


    ChildEventListener mEventListener = null;
    public void select(String room)
    {
        if(mEventListener == null)
        {
            mEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    Message msg = dataSnapshot.getValue(Message.class);
                    Log.i("select()", msg.toString());
                    ChatService.ins.appendContents(msg.toString());
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

}
