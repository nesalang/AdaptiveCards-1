using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Net.Http;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;

namespace AdaptiveCards.Rendering.Wpf
{
    public static class AdaptiveCustomElementRenderer
    {
        public static FrameworkElement Render(AdaptiveCustomElement customElement, AdaptiveRenderContext context)
        {
            try
            {
                if (context.ElementDefinitions != null && context.ElementDefinitions.TryGetValue(customElement.Type, out string url))
                {
                    HttpClient c = new HttpClient();
                    var task = c.GetStringAsync(url);
                    task.Wait();
                    string definition = task.Result;

                    // Super simple data binding
                    foreach (var prop in customElement.AdditionalProperties)
                    {
                        definition = definition.Replace("{" + prop.Key + "}", prop.Value.ToString());
                    }

                    AdaptiveElementDefinition parsedDefinition = JsonConvert.DeserializeObject<AdaptiveElementDefinition>(definition, new JsonSerializerSettings
                    {
                        Converters = { new StrictIntConverter() }
                    });

                    if (parsedDefinition.Element != null)
                    {
                        return context.Render(parsedDefinition.Element);
                    }
                    else
                    {
                        context.Warnings.Add(new AdaptiveWarning(1, "Element definition was empty for " + customElement.Type));
                        return null;
                    }
                }

                context.Warnings.Add(new AdaptiveWarning(1, "No element definition found for " + customElement.Type));
                return null;
            }
            catch (Exception ex)
            {
                return new TextBlock()
                {
                    Text = ex.ToString(),
                    TextWrapping = TextWrapping.Wrap,
                    FontSize = 10
                };
            }
        }
    }
}