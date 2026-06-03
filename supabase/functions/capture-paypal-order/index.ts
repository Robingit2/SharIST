// Follow this setup guide to integrate the Deno language server with your editor:
// https://deno.land/manual/getting_started/setup_your_environment
// This enables autocomplete, go to definition, etc.

// Setup type definitions for built-in Supabase Runtime APIs
import "@supabase/functions-js/edge-runtime.d.ts";
import { withSupabase } from "@supabase/server";

import { serve } from "https://deno.land/std/http/server.ts";

serve(async (req) => {

  const { order_id } = await req.json();

  const auth = await fetch("https://api-m.sandbox.paypal.com/v1/oauth2/token", {
    method: "POST",
    headers: {
      Authorization:
        "Basic " +
        btoa(`${Deno.env.get("PAYPAL_CLIENT_ID")}:${Deno.env.get("PAYPAL_CLIENT_SECRET")}`),
      "Content-Type": "application/x-www-form-urlencoded",
    },
    body: "grant_type=client_credentials",
  });

  const { access_token } = await auth.json();

  const capture = await fetch(
    `https://api-m.sandbox.paypal.com/v2/checkout/orders/${order_id}/capture`,
    {
      method: "POST",
      headers: {
        Authorization: `Bearer ${access_token}`,
        "Content-Type": "application/json",
      },
    }
  );

  const data = await capture.json();

  return new Response(
    JSON.stringify({
      status: data.status,
      isPaid: data.status === "COMPLETED",
      raw: data
    }),
    { headers: { "Content-Type": "application/json" } }
  );
});