package tech.harmonysoft.oss.test.fixture

import tech.harmonysoft.oss.common.ProcessingResult
import tech.harmonysoft.oss.test.binding.DynamicBindingContext
import tech.harmonysoft.oss.test.binding.DynamicBindingKey
import tech.harmonysoft.oss.test.binding.DynamicBindingUtil.DYNAMIC_BOUND_VALUE_REGEX
import tech.harmonysoft.oss.test.fixture.meta.value.FixtureMetaValueMapper
import javax.inject.Named

/**
 * There are use-cases when some values are generated during test execution, e.g. unique ids.
 * We might want to re-use them, for example, a test might register a user, get her id and then send
 * a request from her behalf. Corresponding test steps might look like below.
 *
 * ```
 * Given POST request to /api/v1/register is made
 *
 * And last POST request returns json:
 * """
 * {
 *   "ok": true,
 *   "userId": <bind:user-id>
 * }
 * """
 *
 * When PUT request to /api/v1/order is made with json body
 * """
 * [
 *   {
 *     "userId": <bound:user-id>,
 *     "itemId": "some-item-id"
 *   }
 * ]
 * """
 * ```
 *
 * This class resolves such bindings using [DynamicBindingContext]
 */
@Named
class DynamicBoundValueMapper(
    private val bindingContext: DynamicBindingContext
) : FixtureMetaValueMapper<Any> {

    override val type = CommonTestFixture.TYPE

    override fun map(context: Any, metaValue: String): ProcessingResult<String?, Unit> {
        return DYNAMIC_BOUND_VALUE_REGEX.matchEntire(metaValue)?.let {
            val key = it.groupValues[1]
            bindingContext.getBinding(DynamicBindingKey(key))
        }?.toString()?.let {
            ProcessingResult.success(it)
        } ?: ProcessingResult.failure(Unit)
    }
}