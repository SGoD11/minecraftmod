package com.dhar.zombieassasian.item;

/**
 * Implemented by both LongRangedBucketItem (empty) and
 * LongRangedBucketFilledItem (holding water) so
 * handler/LongRangedBucketHandler.java can recognize "is the player
 * holding some form of the Long-Ranged Bucket" regardless of which exact
 * class/state it's currently in.
 */
public interface ILongRangedTool {
}
