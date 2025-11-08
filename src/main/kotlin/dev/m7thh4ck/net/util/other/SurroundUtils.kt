package dev.m7thh4ck.net.util.other

import dev.m7thh4ck.net.util.Wrapper.world
import dev.m7thh4ck.net.util.extension.fastFloor
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.entity.Entity
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3i

object SurroundUtils {
    val Entity.betterPosition get() = BlockPos(this.blockPos.x, (this.blockPos.y + 0.25).fastFloor(), this.blockPos.z)
    val Entity.flooredPosition get() = BlockPos(blockPos.x, blockPos.y, blockPos.z)

    private val surroundOffset = arrayOf(
        BlockPos(0, -1, 0), // down
        BlockPos(0, 0, -1), // north
        BlockPos(1, 0, 0),  // east
        BlockPos(0, 0, 1),  // south
        BlockPos(-1, 0, 0)  // west
    )

    fun checkHole(entity: Entity) =
        checkHole(entity.flooredPosition)

    fun checkHole(pos: BlockPos): HoleType {
        // Must be a 1 * 3 * 1 empty space
        if (!world.isAir(pos) || !world.isAir(pos.up()) || !world.isAir(
                pos.up().up()
            )
        ) return HoleType.NONE

        var type = HoleType.BEDROCK

        for (offset in surroundOffset) {
            val block = world.getBlockState(pos.add(offset)).block

            if (!checkBlock(block)) {
                type = HoleType.NONE
                break
            }

            if (block != Blocks.BEDROCK) type = HoleType.OBBY
        }

        return type
    }

    private fun checkBlock(block: Block): Boolean {
        return block == Blocks.BEDROCK || block == Blocks.OBSIDIAN || block == Blocks.ENDER_CHEST || block == Blocks.ANVIL
    }
    fun isHole(pos: BlockPos?): Boolean {
        return (isSingleHole(pos)
                || validTwoBlockIndestructible(pos) || validTwoBlockBedrock(
            pos
        )
                || validQuadIndestructible(pos) || validQuadBedrock(
            pos
        ))
    }
    fun isSingleHole(pos: BlockPos?): Boolean {
        return validIndestructible(pos) || validBedrock(
            pos
        )
    }
    fun validTwoBlockIndestructible(pos: BlockPos?): Boolean {
        if (!isReplaceable(pos)) return false
        val addVec: Vec3i = getTwoBlocksDirection(pos) ?: return false

        // If addVec not found -> hole incorrect
        val checkPoses = pos?.let { arrayOf(pos, it.add(addVec)) }
        // Check surround poses of checkPoses
        var wasIndestrictible = false
        if (checkPoses != null) {
            for (checkPos in checkPoses) {
                val downPos = checkPos.down()
                if (isIndestructible(downPos)) wasIndestrictible =
                    true else if (!isBedrock(downPos)) return false
                for (vec in VECTOR_PATTERN) {
                    val reducedPos = checkPos.add(vec)
                    if (isIndestructible(reducedPos)) {
                        wasIndestrictible = true
                        continue
                    }
                    if (pos != null) {
                        if (!isBedrock(reducedPos) && reducedPos != pos && reducedPos != pos.add(
                                addVec
                            )
                        ) return false
                    }
                }
            }
        }
        return wasIndestrictible
    }
    private fun getTwoBlocksDirection(pos: BlockPos?): Vec3i? {
        // Try to get direction
        for (vec in VECTOR_PATTERN) {
            if (pos != null) {
                if (isReplaceable(pos.add(vec))) return vec
            }
        }
        return null
    }
    fun validTwoBlockBedrock(pos: BlockPos?): Boolean {
        if (!isReplaceable(pos)) return false
        val addVec: Vec3i = getTwoBlocksDirection(pos) ?: return false

        // If addVec not found -> hole incorrect
        val checkPoses = pos?.let { arrayOf(pos, it.add(addVec)) }
        // Check surround
        if (checkPoses != null) {
            for (checkPos in checkPoses) {
                val downPos = checkPos.down()
                if (!isBedrock(downPos)) return false
                for (vec in VECTOR_PATTERN) {
                    val reducedPos = checkPos.add(vec)
                    if (!isBedrock(reducedPos) && reducedPos != pos && reducedPos != pos.add(
                            addVec
                        )
                    ) return false
                }
            }
        }
        return true
    }

    fun validQuadIndestructible(pos: BlockPos?): Boolean {
        val checkPoses: List<BlockPos> = getQuadDirection(pos)
            ?: return false
        // If checkPoses not found -> hole incorrect
        var wasIndestrictible = false
        for (checkPos in checkPoses) {
            val downPos = checkPos.down()
            if (isIndestructible(downPos)) {
                wasIndestrictible = true
            } else if (!isBedrock(downPos)) {
                return false
            }
            for (vec in VECTOR_PATTERN) {
                val reducedPos = checkPos.add(vec)
                if (isIndestructible(reducedPos)) {
                    wasIndestrictible = true
                    continue
                }
                if (!isBedrock(reducedPos) && !checkPoses.contains(reducedPos)) {
                    return false
                }
            }
        }
        return wasIndestrictible
    }
    val VECTOR_PATTERN = arrayOf(
        Vec3i(0, 0, 1),
        Vec3i(0, 0, -1),
        Vec3i(1, 0, 0),
        Vec3i(-1, 0, 0)
    )
    fun validQuadBedrock(pos: BlockPos?): Boolean {
        val checkPoses: List<BlockPos> = getQuadDirection(pos)
            ?: return false
        // If checkPoses not found -> hole incorrect
        for (checkPos in checkPoses) {
            val downPos = checkPos.down()
            if (!isBedrock(downPos)) {
                return false
            }
            for (vec in VECTOR_PATTERN) {
                val reducedPos = checkPos.add(vec)
                if (!isBedrock(reducedPos) && !checkPoses.contains(reducedPos)) {
                    return false
                }
            }
        }
        return true
    }

    fun validIndestructible(pos: BlockPos?): Boolean {
        return (!pos?.let { validBedrock(it) }!!
                && (isIndestructible(
            pos.add(
                0,
                -1,
                0
            )
        ) || isBedrock(pos.add(0, -1, 0)))
                && (isIndestructible(
            pos.add(
                1,
                0,
                0
            )
        ) || isBedrock(pos.add(1, 0, 0)))
                && (isIndestructible(
            pos.add(
                -1,
                0,
                0
            )
        ) || isBedrock(pos.add(-1, 0, 0)))
                && (isIndestructible(
            pos.add(
                0,
                0,
                1
            )
        ) || isBedrock(pos.add(0, 0, 1)))
                && (isIndestructible(
            pos.add(
                0,
                0,
                -1
            )
        ) || isBedrock(pos.add(0, 0, -1)))
                && isReplaceable(pos)
                && isReplaceable(pos.add(0, 1, 0))
                && isReplaceable(pos.add(0, 2, 0)))
    }

    private fun getQuadDirection(pos: BlockPos?): List<BlockPos>? {
        // Try to get direction
        val dirList: MutableList<BlockPos> = ArrayList()
        pos?.let { dirList.add(it) }
        if (!pos?.let { isReplaceable(it) }!!) return null
        if (isReplaceable(
                pos.add(
                    1,
                    0,
                    0
                )
            ) && isReplaceable(
                pos.add(
                    0,
                    0,
                    1
                )
            ) && isReplaceable(pos.add(1, 0, 1))
        ) {
            dirList.add(pos.add(1, 0, 0))
            dirList.add(pos.add(0, 0, 1))
            dirList.add(pos.add(1, 0, 1))
        }
        if (isReplaceable(
                pos.add(
                    -1,
                    0,
                    0
                )
            ) && isReplaceable(
                pos.add(
                    0,
                    0,
                    -1
                )
            ) && isReplaceable(pos.add(-1, 0, -1))
        ) {
            dirList.add(pos.add(-1, 0, 0))
            dirList.add(pos.add(0, 0, -1))
            dirList.add(pos.add(-1, 0, -1))
        }
        if (isReplaceable(
                pos.add(
                    1,
                    0,
                    0
                )
            ) && isReplaceable(
                pos.add(
                    0,
                    0,
                    -1
                )
            ) && isReplaceable(pos.add(1, 0, -1))
        ) {
            dirList.add(pos.add(1, 0, 0))
            dirList.add(pos.add(0, 0, -1))
            dirList.add(pos.add(1, 0, -1))
        }
        if (isReplaceable(
                pos.add(
                    -1,
                    0,
                    0
                )
            ) && isReplaceable(
                pos.add(
                    0,
                    0,
                    1
                )
            ) && isReplaceable(pos.add(-1, 0, 1))
        ) {
            dirList.add(pos.add(-1, 0, 0))
            dirList.add(pos.add(0, 0, 1))
            dirList.add(pos.add(-1, 0, 1))
        }
        return if (dirList.size != 4) null else dirList
    }
    private fun isIndestructible(bp: BlockPos): Boolean {
        return if (world == null) false else world!!.getBlockState(bp).block === Blocks.OBSIDIAN || world!!.getBlockState(
            bp
        ).block === Blocks.NETHERITE_BLOCK || world!!.getBlockState(bp).block === Blocks.CRYING_OBSIDIAN || world!!.getBlockState(
            bp
        ).block === Blocks.RESPAWN_ANCHOR
    }

    fun validBedrock(pos: BlockPos?): Boolean {
        return (pos?.let { isBedrock(it.add(0, -1, 0)) } == true
                && isBedrock(pos.add(1, 0, 0))
                && isBedrock(pos.add(-1, 0, 0))
                && isBedrock(pos.add(0, 0, 1))
                && isBedrock(pos.add(0, 0, -1))
                && isReplaceable(pos)
                && isReplaceable(pos.add(0, 1, 0))
                && isReplaceable(pos.add(0, 2, 0)))
    }

    private fun isBedrock(bp: BlockPos): Boolean {
        return if (world == null) false else world!!.getBlockState(bp).block === Blocks.BEDROCK
    }

    private fun isReplaceable(bp: BlockPos?): Boolean {
        return if (world == null) false else world!!.getBlockState(bp).isReplaceable
    }
    enum class HoleType {
        NONE, OBBY, BEDROCK
    }
}