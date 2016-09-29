db.graphItem.update(
    {},{
        $set: {
            "status": "Active",
            "ownerName":"system",
            "groupName":"system"
        }
    },{
        multi : true
    }
);
