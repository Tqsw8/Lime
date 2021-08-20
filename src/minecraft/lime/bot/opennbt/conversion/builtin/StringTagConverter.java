package lime.bot.opennbt.conversion.builtin;

import lime.bot.opennbt.conversion.TagConverter;
import lime.bot.opennbt.tag.builtin.StringTag;

/**
 * A converter that converts between StringTag and String.
 */
public class StringTagConverter implements TagConverter<StringTag, String> {
	@Override
	public String convert(StringTag tag) {
		return tag.getValue();
	}

	@Override
	public StringTag convert(String name, String value) {
		return new StringTag(name, value);
	}
}