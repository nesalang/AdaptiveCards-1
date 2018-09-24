#include "pch.h"
#include "Fact.h"
#include "DateTimePreparser.h"
#include "ParseUtil.h"

using namespace AdaptiveSharedNamespace;

Fact::Fact()
{
}

Fact::Fact(std::string const &title, std::string const &value) : 
    m_title(title), m_value(value)
{
}

std::shared_ptr<Fact> Fact::Deserialize(
    std::shared_ptr<ElementParserRegistration>,
    std::shared_ptr<ActionParserRegistration>,
    std::vector<std::shared_ptr<AdaptiveCardParseWarning>>&,
    const Json::Value& json)
{
    std::string title = ParseUtil::GetString(json, AdaptiveCardSchemaKey::Title, true);
    std::string value = ParseUtil::GetString(json, AdaptiveCardSchemaKey::Value, true);

    auto fact = std::make_shared<Fact>(title, value);
    return fact;
}

std::shared_ptr<Fact> Fact::DeserializeFromString(
    std::shared_ptr<ElementParserRegistration> elementParserRegistration,
    std::shared_ptr<ActionParserRegistration> actionParserRegistration,
    std::vector<std::shared_ptr<AdaptiveCardParseWarning>>& warnings,
    const std::string& jsonString)
{
    return Fact::Deserialize(elementParserRegistration, actionParserRegistration, warnings, ParseUtil::GetJsonValueFromString(jsonString));
}

std::string Fact::Serialize()
{
    Json::FastWriter writer;
    return writer.write(SerializeToJsonValue());
}

Json::Value Fact::SerializeToJsonValue()
{
    Json::Value root;
    root[AdaptiveCardSchemaKeyToString(AdaptiveCardSchemaKey::Title)] = GetTitle();
    root[AdaptiveCardSchemaKeyToString(AdaptiveCardSchemaKey::Value)] = GetValue();

    return root;
}

std::string Fact::GetTitle() const
{
    return m_title;
}

void Fact::SetTitle(const std::string &value)
{
    m_title = value;
}

std::string Fact::GetValue() const
{
    return m_value;
}

void Fact::SetValue(const std::string &value)
{
    m_value = value;
}

DateTimePreparser Fact::GetValueForDateParsing() const
{
    return DateTimePreparser(m_value);
}

std::string Fact::GetLanguage() const
{
    return m_language;
}

void Fact::SetLanguage(const std::string& value)
{
    m_language = value;
}