// Follow this setup guide to integrate the Deno language server with your editor:
// https://deno.land/manual/getting_started/setup_your_environment
// This enables autocomplete, go to definition, etc.

// Setup type definitions for built-in Supabase Runtime APIs
import "@supabase/functions-js/edge-runtime.d.ts";
import { withSupabase } from "@supabase/server";

import { serve } from "https://deno.land/std/http/server.ts";

serve(async (req) => {
  const PAYPAL_CLIENT_ID = Deno.env.get("PAYPAL_CLIENT_ID");
  const PAYPAL_CLIENT_SECRET = Deno.env.get("PAYPAL_CLIENT_SECRET");

  // 1. Get PayPal Access Token
  const auth = await fetch("https://api-m.sandbox.paypal.com/v1/oauth2/token", {
    method: "POST",
    headers: {
      "Authorization": "Basic " + btoa(`${PAYPAL_CLIENT_ID}:${PAYPAL_CLIENT_SECRET}`),
      "Content-Type": "application/x-www-form-urlencoded",
    },
    body: "grant_type=client_credentials",
  });

  const authData = await auth.json();
  const accessToken = authData.access_token;

  // 2. Create Order
  const order = await fetch("https://api-m.sandbox.paypal.com/v2/checkout/orders", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      "Authorization": `Bearer ${accessToken}`,
    },
    body: JSON.stringify({
      intent: "CAPTURE",
      purchase_units: [
        {
          amount: {
            currency_code: "USD",
            value: "100.00",
          },
        },
      ],
      application_context: {
        return_url: "myapp://paypal-success",
        cancel_url: "myapp://paypal-cancel",
        landing_page: "LOGIN",
        user_action: "PAY_NOW",
        shipping_preference: "NO_SHIPPING"
      }
    }),
  });

  const orderData = await order.json();
  console.log("TEST",JSON.stringify(orderData, null, 2));
  return new Response(JSON.stringify(orderData), {
    headers: { "Content-Type": "application/json" },
    status: 200,
  });
});