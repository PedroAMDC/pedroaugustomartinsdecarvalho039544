export default function Loading() {
  return (
    <div className="container mx-auto p-8">
      <div className="animate-pulse">
        <div className="h-8 bg-muted rounded w-48 mb-8" />
        <div className="space-y-4">
          {[...Array(5)].map((_, i) => (
            <div key={i} className="h-16 bg-muted rounded" />
          ))}
        </div>
      </div>
    </div>
  );
}
